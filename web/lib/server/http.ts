export class RequestError extends Error {
  constructor(
    message: string,
    public status = 400,
  ) {
    super(message);
  }
}
export function json(data: unknown, status = 200) {
  return Response.json(data, {
    status,
    headers: {
      "Cache-Control": "no-store",
      "X-Content-Type-Options": "nosniff",
    },
  });
}
export async function readJson(
  req: Request,
  limit = 220_000,
): Promise<Record<string, unknown>> {
  if (!req.headers.get("content-type")?.includes("application/json"))
    throw new RequestError(
      "Send JSON with Content-Type: application/json.",
      415,
    );
  if (Number(req.headers.get("content-length")) > limit)
    throw new RequestError("This request is too large.", 413);
  const reader = req.body?.getReader();
  if (!reader) throw new RequestError("A JSON body is required.");
  const chunks: Uint8Array[] = [];
  let size = 0;
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    size += value.byteLength;
    if (size > limit) {
      await reader.cancel();
      throw new RequestError("This request is too large.", 413);
    }
    chunks.push(value);
  }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const chunk of chunks) {
    bytes.set(chunk, offset);
    offset += chunk.length;
  }
  let body: unknown;
  try {
    body = JSON.parse(new TextDecoder().decode(bytes));
  } catch {
    throw new RequestError("The request contains invalid JSON.");
  }
  if (!body || typeof body !== "object" || Array.isArray(body))
    throw new RequestError("A JSON object is required.");
  return body as Record<string, unknown>;
}
export function errorResponse(
  error: unknown,
  fallback = "The service is unavailable. Please try again.",
) {
  if (error instanceof RequestError)
    return json({ error: error.message }, error.status);
  if (
    error instanceof Error &&
    ["TimeoutError", "AbortError", "APIConnectionTimeoutError"].includes(
      error.name,
    )
  )
    return json(
      {
        error: "The request timed out. Try a shorter message or faster model.",
      },
      504,
    );
  const status =
    error && typeof error === "object" && "status" in error
      ? Number(error.status)
      : 0;
  if (status === 429)
    return json(
      {
        error:
          "The AI provider is busy or its usage limit was reached. Please try again later.",
      },
      429,
    );
  // Never expose upstream errors: they may contain credentials or request content.
  return json({ error: fallback }, 502);
}
