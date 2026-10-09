import { getUser } from "@netlify/identity";
import { defaultModel, type ItemKind, type WorkspaceItem } from "./catalog";
export async function api<T>(
  url: string,
  init?: RequestInit,
  expectedUserId?: string,
): Promise<T> {
  if (url.startsWith("/api/workspace")) {
    const current = await getUser();
    if (expectedUserId && current?.id !== expectedUserId)
      throw new Error(
        "Your account changed. Please retry from the current workspace.",
      );
  }
  const response = await fetch(url, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
    cache: "no-store",
  });
  const data = await response.json().catch(() => ({
    error: "The service returned an unexpected response. Please retry.",
  }));
  if (!response.ok)
    throw new Error(data.error || `Request failed (${response.status}).`);
  return data as T;
}
export function newItem(
  kind: ItemKind = "chat",
  agent = "general",
): WorkspaceItem {
  return {
    id: crypto.randomUUID(),
    kind,
    title: kind === "chat" ? "New conversation" : "",
    body: "",
    completed: false,
    dueDate: "",
    model: defaultModel,
    agent,
    messages: [],
    contextId: "",
    updatedAt: new Date().toISOString(),
  };
}
export function download(
  name: string,
  content: string | Blob,
  type = "text/plain;charset=utf-8",
) {
  const url = URL.createObjectURL(
    content instanceof Blob ? content : new Blob([content], { type }),
  );
  const link = document.createElement("a");
  link.href = url;
  link.download = name;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10_000);
}
export async function copyText(text: string) {
  if (!navigator.clipboard)
    throw new Error(
      "Clipboard is unavailable in this browser. Select and copy the text manually.",
    );
  await navigator.clipboard.writeText(text);
}
