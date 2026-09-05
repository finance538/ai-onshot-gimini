# OneShot DeepFind Research Agent v1

This is an independent, bounded domain-research workflow, not the DeepFind website's original conversational agent. It does not implement general web search, private-person lookups, dorks, vulnerability exploitation or autonomous browsing.

## Entry points

- `/agents`: research agent directory, linked from a persistent workspace shortcut.
- `/agents/deepfind`: bilingual Arabic/English client; theme follows the workspace.
- `/api/agents/deepfind`: authenticated status/run endpoint.
- `/api/agents/deepfind/session`: scoped HttpOnly session issuance/revocation.

## Workflow

Quick: DNS and WHOIS. Deep: additionally HTTP headers, technology detection and at most 10 Wayback results. At most five DeepFind requests and one optional Gemini summary call. The model cannot choose new endpoints or targets. Evidence preserves per-tool status, timestamps, sanitized response and source identifier; failed steps are never represented as facts. Contact and credential fields are removed. The model summary is advisory and must be checked against evidence. Presence of citation IDs is not a guarantee of factual accuracy.

Reports can be exported as JSON. Saving on the device is opt-in, limited to ten reports. No cloud history/database is implemented for this feature. Start/end events go to server runtime logs without API secrets, query text, or raw evidence.

## Configuration

Configure `DEEPFIND_API_KEY` and `RESEARCH_ACCESS_CODE` as server-only Secrets, production context and functions scope. The latter should be a separate random value with at least 32 characters; it is NOT the provider API key. Existing `GOOGLE_GENERATIVE_AI_API_KEY` and `GOOGLE_MODEL` are reused only for optional summarization.

`RESEARCH_ALLOWED_DOMAINS` is an exact comma-separated allowlist (default `1shotcam.com`). No wildcard or subdomain expansion. `RESEARCH_APP_ORIGIN` defaults to `https://oneshot-ai-gpt.netlify.app`.

DeepFind's public docs at https://deepfind.me/api specify authenticated access but do not state the exact header format in the retrieved text. `DEEPFIND_AUTH_MODE` defaults to `x-api-key`; `bearer` is configurable. Do not declare the service connected until a real authenticated request succeeds. There is no automatic auth-mode guessing.

The session is valid for eight hours, signed and scoped to the research API. Origin checking and an exact domain allowlist are enforced server-side. A missing secret fails closed. Process-local burst limits are defense in depth, NOT global/distributed spend limits. Before use by multiple users, add full workspace authentication, durable rate limiting, and retained audit storage. Other existing workspace endpoints are not modified or secured by this patch.

## Verification

The pure workflow, validation, auth and response limiting were tested with mocked upstream responses. A complete Netlify production build and real DeepFind authentication must be verified separately. Do not describe mocked outputs as live provider results. The speed/cost relative to the original DeepFind agent is not established.
