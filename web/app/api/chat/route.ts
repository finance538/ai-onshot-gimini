type Message = { role: 'user' | 'assistant'; content: string };
export const runtime = 'nodejs';

const API_BASE = process.env.ONESHOT_API_BASE_URL || 'https://api.1shotcam.com';

export async function POST(req: Request) {
  try {
    const body = await req.json() as { messages?: Message[] };
    const messages = Array.isArray(body.messages) ? body.messages.slice(-20) : [];
    if (!messages.length) {
      return Response.json({ error: 'No messages supplied.' }, { status: 400 });
    }

    const upstream = await fetch(`${API_BASE}/ai/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Cookie: req.headers.get('cookie') || '',
      },
      body: JSON.stringify({ messages }),
      cache: 'no-store',
      signal: AbortSignal.timeout(125000),
    });

    const text = await upstream.text();
    let data: Record<string, unknown> = {};
    try { data = text ? JSON.parse(text) : {}; } catch { data = { error: text || 'Invalid upstream response' }; }

    if (!upstream.ok) {
      return Response.json(
        { error: data.message || data.error || 'OneShot AI request failed.', code: data.code },
        { status: upstream.status }
      );
    }

    return Response.json(data);
  } catch {
    return Response.json({ error: 'Chat request failed.' }, { status: 500 });
  }
}
