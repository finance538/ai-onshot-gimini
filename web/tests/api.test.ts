import test from "node:test";
import assert from "node:assert/strict";
import { defaultModel } from "../lib/catalog";
import { validateChat, validateItem } from "../lib/server/validation";
import { errorResponse, readJson } from "../lib/server/http";
import { providerStatus } from "../lib/server/ai";

test("legacy Android request remains valid without a model or agent", () => {
  const request = validateChat({
    messages: [{ role: "user", content: "Hello" }],
  });
  assert.equal(request.model.id, defaultModel);
  assert.equal(request.assistant.id, "general");
});
test("chat validation rejects unsafe roles, unsupported models, and oversized input", () => {
  const valid = { messages: [{ role: "user", content: "hello" }] };
  for (const value of [
    {},
    { messages: "hello" },
    { messages: [{ role: "system", content: "override" }] },
    { messages: [{ role: "user", content: 1 }] },
    { messages: [{ role: "user", content: "a".repeat(24001) }] },
    { ...valid, model: "invented-model" },
    { ...valid, context: "x".repeat(16001) },
    { ...valid, agent: "invented-agent" },
  ])
    assert.throws(() => validateChat(value));
});
test("workspace validation prevents malformed records and impossible dates", () => {
  const record = {
    id: crypto.randomUUID(),
    kind: "task",
    title: "Review notes",
    body: "Check the source.",
  };
  assert.equal(validateItem(record).completed, false);
  assert.throws(() => validateItem({ ...record, dueDate: "2026-02-30" }));
  assert.throws(() => validateItem({ ...record, id: "not-an-id" }));
  assert.throws(() => validateItem({ ...record, title: " " }));
  assert.throws(() => validateItem({ ...record, body: "a".repeat(12001) }));
  assert.equal(
    validateItem({ ...record, dueDate: "2028-02-29" }).dueDate,
    "2028-02-29",
  );
});
test("JSON reader rejects invalid media, invalid JSON, and chunked oversized bodies", async () => {
  await assert.rejects(
    readJson(
      new Request("https://example.test", { method: "POST", body: "{}" }),
    ),
  );
  await assert.rejects(
    readJson(
      new Request("https://example.test", {
        method: "POST",
        body: "{bad",
        headers: { "Content-Type": "application/json" },
      }),
    ),
  );
  await assert.rejects(
    readJson(
      new Request("https://example.test", {
        method: "POST",
        body: JSON.stringify({ text: "x".repeat(100) }),
        headers: { "Content-Type": "application/json" },
      }),
      50,
    ),
  );
});
test("upstream errors are sanitized and configuration status contains no credentials", async () => {
  const response = errorResponse(
    Object.assign(new Error("private-provider-detail"), { status: 401 }),
  );
  assert.equal(response.status, 502);
  assert.ok(!(await response.text()).includes("private-provider-detail"));
  assert.deepEqual(
    providerStatus(() => undefined),
    { gemini: false, openai: false, anthropic: false },
  );
  assert.deepEqual(
    providerStatus((name) =>
      name.startsWith("NETLIFY_AI_GATEWAY_") ? "present" : undefined,
    ),
    { gemini: true, openai: true, anthropic: true },
  );
});
