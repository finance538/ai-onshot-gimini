import type { Config, Context } from '@netlify/functions';

type Message = { role: 'user' | 'assistant'; content: string };

export default async (req: Request, _context: Context) => {
  if (req.method !== 'POST') {
    return Response.json({ error: 'Method not allowed.' }, { status: 405 });
  }

  try {
    const body = (await req.json()) as { messages?: Message[] };
    const messages = Array.isArray(body.messages) ? body.messages.slice(-20) : [];
    if (!messages.length) {
      return Response.json({ error: 'No messages supplied.' }, { status: 400 });
    }

    const key = Netlify.env.get('GOOGLE_GENERATIVE_AI_API_KEY');
    const model = Netlify.env.get('GOOGLE_MODEL') || 'gemini-3.6-flash';
    if (!key) {
      return Response.json({ error: 'Gemini API key is not configured on the server.' }, { status: 503 });
    }

    const contents = messages.map((m) => ({
      role: m.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: m.content.slice(0, 12000) }],
    }));

    const upstream = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-goog-api-key': key,
        },
        body: JSON.stringify({
          systemInstruction: {
            parts: [
              {
                text: 'You are OneShot AI, the operational AI workspace for OneShot. Be practical, concise, bilingual when asked, and do not claim tools you do not have.',
              },
            ],
          },
          contents,
          generationConfig: { temperature: 0.5, maxOutputTokens: 2000 },
        }),
        signal: AbortSignal.timeout(30000),
      },
    );

    if (!upstream.ok) {
      return Response.json({ error: `Gemini request failed (${upstream.status}).` }, { status: 502 });
    }

    const data = (await upstream.json()) as {
      candidates?: { content?: { parts?: { text?: string }[] } }[];
    };
    const text = data.candidates?.[0]?.content?.parts?.map((p) => p.text || '').join('').trim();
    if (!text) {
      return Response.json({ error: 'Gemini returned an empty response.' }, { status: 502 });
    }

    return Response.json({ text, model });
  } catch {
    return Response.json({ error: 'Chat request failed.' }, { status: 500 });
  }
};

export const config: Config = {
  path: '/api/chat',
};
