import type { Config } from "@netlify/functions";
import { getUser } from "@netlify/identity";
import { and, desc, eq } from "drizzle-orm";
import { getDb } from "../../db/index.js";
import { workspaceItems } from "../../db/schema.js";
import {
  errorResponse,
  json,
  readJson,
  RequestError,
} from "../../lib/server/http";
import { isId, validateItem } from "../../lib/server/validation";
export default async (req: Request) => {
  if (!["GET", "PUT", "DELETE"].includes(req.method))
    return json({ error: "Method not allowed." }, 405);
  try {
    const user = await getUser();
    if (!user)
      return json({ error: "Sign in to save and access your workspace." }, 401);
    if (req.method !== "GET") {
      const origin = req.headers.get("origin");
      if (origin && origin !== new URL(req.url).origin)
        throw new RequestError(
          "This request must come from your workspace.",
          403,
        );
    }
    const db = getDb();
    const params = new URL(req.url).searchParams;
    const id = params.get("id");
    const owner = eq(workspaceItems.userId, user.id);
    if (req.method === "GET") {
      if (id) {
        if (!isId(id)) throw new RequestError("Invalid item ID.");
        const [item] = await db
          .select()
          .from(workspaceItems)
          .where(and(owner, eq(workspaceItems.id, id)));
        return item ? json({ item }) : json({ error: "Item not found." }, 404);
      }
      const offset = Number(params.get("offset") || 0);
      if (!Number.isSafeInteger(offset) || offset < 0 || offset > 1_000_000)
        throw new RequestError("Invalid page offset.");
      const items = await db
        .select({
          id: workspaceItems.id,
          kind: workspaceItems.kind,
          title: workspaceItems.title,
          body: workspaceItems.body,
          completed: workspaceItems.completed,
          dueDate: workspaceItems.dueDate,
          model: workspaceItems.model,
          agent: workspaceItems.agent,
          updatedAt: workspaceItems.updatedAt,
        })
        .from(workspaceItems)
        .where(owner)
        .orderBy(desc(workspaceItems.updatedAt), desc(workspaceItems.id))
        .limit(101)
        .offset(offset);
      return json({
        items: items.slice(0, 100),
        nextOffset: items.length > 100 ? offset + 100 : null,
      });
    }
    if (req.method === "DELETE") {
      if (!isId(id)) throw new RequestError("Invalid item ID.");
      const removed = await db
        .delete(workspaceItems)
        .where(and(owner, eq(workspaceItems.id, id)))
        .returning({ id: workspaceItems.id });
      return removed.length
        ? json({ deleted: true })
        : json({ error: "Item not found." }, 404);
    }
    const data = validateItem(await readJson(req));
    const [item] = await db
      .insert(workspaceItems)
      .values({ ...data, userId: user.id })
      .onConflictDoUpdate({
        target: workspaceItems.id,
        set: { ...data, updatedAt: new Date() },
        setWhere: owner,
      })
      .returning();
    return item ? json({ item }) : json({ error: "Item not found." }, 404);
  } catch (error) {
    return errorResponse(
      error,
      "Saved work is temporarily unavailable. Your current draft remains open; please retry.",
    );
  }
};
export const config: Config = { path: "/api/workspace" };
