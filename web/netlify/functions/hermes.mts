import type { Config, Context } from '@netlify/functions';

type HermesRequest = { message?: string; contextId?: string };
type A2APart = { text?: string; mediaType?: string; [key: string]: unknown };
type A2AMessage = { role?: string; parts?: A2APart[]; contextId?: string; [key: string]: unknown };

function textFromMessage(value: unknown): string {
  if (!value || typeof value !== 'object') return '';
  const v = value as Record<string, unknown>;
  const parts = Array.isArray(v.parts) ? v.parts : [];
  return parts.map((p) => p && typeof p === 'object' && typeof (p as A2APart).text === 'string' ? (p as A2APart).text : '').filter(Boolean).join('\n').trim();
}

function textFromResult(result: unknown): { text: string; contextId?: string; taskId?: string; state?: string } {
  if (!result || typeof result !== 'object') return { text: String(result ?? '') };
  let payload = result as Record<string, unknown>;
  if (payload.task && typeof payload.task === 'object') payload = payload.task as Record<string, unknown>;
  else if (payload.message && typeof payload.message === 'object') payload = payload.message as Record<string, unknown>;

  const artifacts = Array.isArray(payload.artifacts) ? payload.artifacts : [];
  for (const artifact of artifacts) {
    const text = textFromMessage(artifact);
    if (text) return {
      text,
      contextId: typeof payload.contextId === 'string' ? payload.contextId : undefined,
      taskId: typeof payload.id === 'string' ? payload.id : undefined,
      state: typeof (payload.status as Record<string, unknown> | undefined)?.state === 'string' ? String((payload.status as Record<string, unknown>).state) : undefined,
    };
  }

  const status = payload.status && typeof payload.status === 'object' ? payload.status as Record<string, unknown> : undefined;
  const statusText = textFromMessage(status?.message);
  const directText = textFromMessage(payload);
  return {
    text: statusText || directText,
    contextId: typeof payload.contextId === 'string' ? payload.contextId : undefined,
    taskId: typeof payload.id === 'string' ? payload.id : undefined,
    state: typeof status?.state === 'string' ? status.state : undefined,
  };
}

export default async (req: Request, _context: Context) => {
  if (req.method !== 'POST') return Response.json({ error: 'Method not allowed.' }, { status: 405 });

  try {
    const body = (await req.json()) as HermesRequest;
    const message = body.message?.trim();
    if (!message) return Response.json({ error: 'Message is required.' }, { status: 400 });

    const baseUrl = Netlify.env.get('HERMES_A2A_URL')?.replace(/\/$/, '');
    const token = Netlify.env.get('HERMES_A2A_TOKEN');
    if (!baseUrl || !token) return Response.json({ error: 'Hermes A2A is not configured on the server.' }, { status: 503 });

    const contextId = body.contextId?.trim() || `ctx-${crypto.randomUUID().replace(/-/g, '').slice(0, 16)}`;
    const rpcId = `task-${crypto.randomUUID().replace(/-/g, '').slice(0, 16)}`;
    const rpcBody = {
      jsonrpc: '2.0',
      id: rpcId,
      method: 'SendMessage',
      params: {
        message: {
          role: 'ROLE_USER',
          parts: [{ text: message.slice(0, 24000), mediaType: 'text/plain' }],
          messageId: crypto.randomUUID().replace(/-/g, ''),
          contextId,
        },
      },
    };

    const upstream = await fetch(baseUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'A2A-Version': '1.0',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(rpcBody),
      signal: AbortSignal.timeout(120000),
      cache: 'no-store',
    });

    const raw = await upstream.text();
    let data: any;
    try { data = JSON.parse(raw); } catch { data = null; }

    if (!upstream.ok) return Response.json({ error: `Hermes A2A request failed (${upstream.status}).` }, { status: 502 });
    if (!data) return Response.json({ error: 'Hermes returned a non-JSON response.' }, { status: 502 });
    if (data.error) return Response.json({ error: data.error.message || 'Hermes A2A returned an error.' }, { status: 502 });

    const parsed = textFromResult(data.result);
    if (!parsed.text) return Response.json({ error: 'Hermes returned an empty response.', taskId: parsed.taskId, state: parsed.state }, { status: 502 });

    return Response.json({ text: parsed.text, agent: 'OneShot-Hermes', contextId: parsed.contextId || contextId, taskId: parsed.taskId, state: parsed.state });
  } catch (error) {
    const message = error instanceof Error && error.name === 'TimeoutError' ? 'Hermes task timed out.' : 'Hermes request failed.';
    return Response.json({ error: message }, { status: 500 });
  }
};

export const config: Config = { path: '/api/hermes' };
