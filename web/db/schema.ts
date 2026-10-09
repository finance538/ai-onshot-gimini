import {
  pgTable,
  uuid,
  text,
  boolean,
  jsonb,
  timestamp,
  index,
} from "drizzle-orm/pg-core";
import type { Message } from "../lib/catalog";
export const workspaceItems = pgTable(
  "workspace_items",
  {
    id: uuid("id").primaryKey(),
    userId: text("user_id").notNull(),
    kind: text("kind").notNull(),
    title: text("title").notNull(),
    body: text("body").notNull().default(""),
    completed: boolean("completed").notNull().default(false),
    dueDate: text("due_date").notNull().default(""),
    model: text("model").notNull().default("gemini-2.5-flash"),
    agent: text("agent").notNull().default("general"),
    messages: jsonb("messages").$type<Message[]>().notNull().default([]),
    contextId: text("context_id").notNull().default(""),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [
    index("workspace_items_owner_updated").on(table.userId, table.updatedAt),
  ],
);
