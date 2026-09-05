# OneShot AI - Android + Web

OneShot AI is a cloud-first AI workspace with an Android app built in Kotlin/Jetpack Compose and a web companion built with Next.js under `web/`.

## Android

- Kotlin + Jetpack Compose + Material Design 3
- Room persistence
- Gemini/model routing architecture
- Projects, agents, tasks, knowledge, tools and DeepFind research workflows

## Web

- Next.js App Router in `web/`
- Responsive mobile/desktop layout
- Arabic/English and dark/light modes
- Persistent local conversations
- Chat via server-side Gemini API route
- Projects, Agents, Tasks, Knowledge and Tools workspace views
- Netlify configuration at repository root (`netlify.toml`) with `web/` as the build base

## Environment variables for Web

Set these on the hosting provider, never in committed source:

- `GOOGLE_GENERATIVE_AI_API_KEY`
- `GOOGLE_MODEL` (defaults to `gemini-3.6-flash`)

## Local Web development

```bash
cd web
npm install
npm run dev
```

## Netlify

Netlify reads `netlify.toml`, builds the `web/` directory with `npm run build`, and deploys the Next.js application.
