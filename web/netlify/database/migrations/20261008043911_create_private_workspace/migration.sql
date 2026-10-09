CREATE TABLE "workspace_items" (
	"id" uuid PRIMARY KEY,
	"user_id" text NOT NULL,
	"kind" text NOT NULL,
	"title" text NOT NULL,
	"body" text DEFAULT '' NOT NULL,
	"completed" boolean DEFAULT false NOT NULL,
	"due_date" text DEFAULT '' NOT NULL,
	"model" text DEFAULT 'gemini-2.5-flash' NOT NULL,
	"agent" text DEFAULT 'general' NOT NULL,
	"messages" jsonb DEFAULT '[]' NOT NULL,
	"context_id" text DEFAULT '' NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE INDEX "workspace_items_owner_updated" ON "workspace_items" ("user_id","updated_at");