import {
  defaultModel,
  findAssistant,
  models,
  type Message,
  type WorkspaceItem,
} from "../catalog";
import { RequestError } from "./http";
export function validateMessages(value: unknown, max = 80): Message[] {
  if (!Array.isArray(value) || !value.length || value.length > max)
    throw new RequestError(`Supply between 1 and ${max} messages.`);
  let total = 0;
  const messages = value.map((m) => {
    if (
      !m ||
      (m.role !== "user" && m.role !== "assistant") ||
      typeof m.content !== "string" ||
      !m.content.trim()
    )
      throw new RequestError(
        "Each message needs a user or assistant role and non-empty text.",
      );
    if (m.content.length > 24_000)
      throw new RequestError("A message cannot exceed 24,000 characters.");
    total += m.content.length;
    return {
      role: m.role,
      content: m.content,
      ...(m.role === "assistant" &&
      (m.model === "hermes" || models.some((model) => model.id === m.model))
        ? { model: m.model }
        : {}),
    } as Message;
  });
  if (total > 120_000)
    throw new RequestError(
      "This conversation is too long. Start a new conversation.",
      413,
    );
  return messages;
}
export function validateChat(body: Record<string, unknown>) {
  const messages = validateMessages(body.messages).map(({ role, content }) => ({
    role,
    content,
  }));
  if (messages.at(-1)?.role !== "user")
    throw new RequestError("The last message must be from the user.");
  const model = models.find(
    (m) => m.id === (body.model === undefined ? defaultModel : body.model),
  );
  if (!model) throw new RequestError("Choose a supported model.");
  const assistant = findAssistant(
    typeof body.agent === "string" ? body.agent : "general",
  );
  if (!assistant) throw new RequestError("Choose a supported assistant.");
  if (
    body.context !== undefined &&
    (typeof body.context !== "string" || body.context.length > 16_000)
  )
    throw new RequestError("Project context cannot exceed 16,000 characters.");
  return {
    messages,
    model,
    assistant,
    context: typeof body.context === "string" ? body.context : "",
  };
}
export const isId = (value: unknown): value is string =>
  typeof value === "string" &&
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value,
  );
export function validateItem(
  body: Record<string, unknown>,
): Omit<WorkspaceItem, "updatedAt"> {
  if (!isId(body.id)) throw new RequestError("A valid item ID is required.");
  if (!["chat", "project", "task", "note"].includes(String(body.kind)))
    throw new RequestError("Unknown item type.");
  if (
    typeof body.title !== "string" ||
    !body.title.trim() ||
    body.title.length > 120
  )
    throw new RequestError("Use a title between 1 and 120 characters.");
  if (typeof body.body !== "string" || body.body.length > 12_000)
    throw new RequestError("Content cannot exceed 12,000 characters.");
  const dueDate = typeof body.dueDate === "string" ? body.dueDate : "";
  if (
    dueDate &&
    (!/^\d{4}-\d{2}-\d{2}$/.test(dueDate) ||
      !Number.isFinite(Date.parse(dueDate)) ||
      new Date(dueDate).toISOString().slice(0, 10) !== dueDate)
  )
    throw new RequestError("Choose a valid due date.");
  const model = typeof body.model === "string" ? body.model : defaultModel;
  if (!models.some((m) => m.id === model))
    throw new RequestError("Choose a supported model.");
  const agent = typeof body.agent === "string" ? body.agent : "general";
  if (agent !== "hermes" && !findAssistant(agent))
    throw new RequestError("Choose a supported assistant.");
  const contextId = typeof body.contextId === "string" ? body.contextId : "";
  if (contextId.length > 256)
    throw new RequestError("Invalid conversation context.");
  const messages =
    body.kind === "chat" && Array.isArray(body.messages) && body.messages.length
      ? validateMessages(body.messages)
      : [];
  return {
    id: body.id,
    kind: body.kind as WorkspaceItem["kind"],
    title: body.title.trim(),
    body: body.body,
    completed: body.completed === true,
    dueDate,
    model,
    agent,
    messages,
    contextId,
  };
}
