# OneShot workspace

The workspace includes 27 deterministic browser utilities and 15 specialized AI assistants, with 11 allowlisted models from Google, OpenAI, and Anthropic. Browser utilities process inputs on the device. AI assistants send conversation text and any explicitly selected reference material to the chosen provider. They do not have live web access or access to email, calendars, repositories, or other external apps.

## Running locally

Use Node 22.12 or newer, install with `npm ci`, then run `netlify dev --port 8889`. Netlify Dev supplies the function runtime and the `/api/*` routes; running Next.js alone does not serve these functions.

Run `npm run typecheck` and `npm test` for static checks and utility/API validation. With Netlify Dev running, `npm run test:browser` checks browser workflows. Install the Playwright Chromium browser before the first browser test run. The authenticated browser test uses explicit service fixtures; it does not create accounts, send email, or apply database migrations.

## AI connections

Netlify AI Gateway supplies credentials at runtime. Supported model IDs are allowlisted in `lib/catalog.ts`; arbitrary client-supplied model IDs are rejected. Explicit provider configuration remains supported. The legacy Google credential is used only when neither the gateway nor the current Gemini configuration is available, so an old credential cannot silently bypass a working gateway.

`GET /api/status` returns only configuration flags and the public model catalog. “Configured” indicates available credentials, not a guarantee of provider uptime or remaining credits. Chat requests have bounded input/output, timeouts below the Netlify function limit, and per-IP platform rate limits. Provider errors are sanitized.

## Existing Android clients

The original routes and response fields remain available:

```http
POST /api/chat
Content-Type: application/json

{"messages":[{"role":"user","content":"Hello"}]}
```

Successful responses contain `text`, with additive `model` and `agent` metadata. Model and assistant selections are optional. `POST /api/hermes` continues to accept `message` and an optional `contextId`, and preserves the existing A2A request format. Requests require JSON content type. HTTPS is required for the Hermes service.

Hermes needs a configured service URL and service credential. It remains unavailable in the interface until both are present. The app preserves Hermes conversation context and does not claim to complete pending external tasks without a text result. Hermes execution still depends on the independently hosted agent and its tools.

The repository contains no Android source or APK, so native APK behavior, signing, and WebView download handling cannot be verified here. The API contract and responsive browser interface were checked. No new APK was generated.

## Private saved work

Guest chats are temporary and clearly marked as unsaved. Signed-in users can save conversations, projects, task checklists, and knowledge notes. Every database query and mutation is scoped to the verified Netlify Identity user. Model prompts cannot access another user's records. Project and note context enters a conversation only when the user selects “Use in chat.” Task due dates are organizational metadata; tasks do not schedule jobs or send reminders.

Netlify Identity uses `@netlify/identity`, including confirmation, recovery, and invite callbacks. The Identity activation script has been run. New accounts require email confirmation unless the site settings enable autoconfirm.

Structured records use Netlify Database through the Drizzle adapter. `db/schema.ts` is the schema source, and `netlify/database/migrations` contains the generated migration. Netlify applies it automatically during deployment. Do not apply the migration manually. Private storage becomes available after deployment activates Identity and applies the database schema.

The old browser conversation store is read only for an explicit import or backup download under Settings. Original browser data is retained after import. New conversation data is saved in Netlify Database, and is never silently substituted with browser storage when a save fails. Results remain open and saving can be retried or the conversation exported.

Saved work is fetched in pages of 100 records, with a control to load older work. Conversation message bodies are fetched only when a conversation is opened. Inputs and notes have size limits to keep serverless requests bounded.

## Domain

At the time of review, `ai.1shotcam.com` had a CNAME pointing to `oneshot-ai-workspace.netlify.app`, which is a different site from this project's `oneshot-ai-gpt.netlify.app`. No DNS, domain ownership, or redirect changes were made. Moving it requires identifying the intended destination and arranging the custom domain and TLS on that site. Existing routing remains intact.

## Validation

All 11 catalog models returned non-empty text in live connection checks. The legacy chat request and all three provider families also returned successful responses through the local Netlify endpoint. Hermes was not configured, and its unavailable/error paths were checked instead. Real authenticated database writes and email delivery require the deployment activation described above.

The framework remained on Next.js 15 with its latest patch. A patched PostCSS version is pinned through an override to address transitive advisories without a framework major upgrade. The final dependency audit, type check, unit tests, and browser test results are recorded in the change summary.
