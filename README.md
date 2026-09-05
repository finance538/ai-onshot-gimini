# OneShot AI - Android

OneShot AI is the cloud-first AI workspace for Android, built with Kotlin, Jetpack Compose, and Material Design 3. It provides a provider-independent Model Router, persistent project workspaces, specialized AI agents, autonomous tasks, company memory, and external tool integrations.

## Architecture

- **UI Framework:** Android Jetpack Compose + Material Design 3
- **Language:** Kotlin
- **Persistence:** Room SQLite Database with Flow queries
- **Asynchronous Flow:** Kotlin Coroutines & StateFlow
- **AI Gateway & Gemini:** Direct Gemini API client (`gemini-2.5-flash`) with model routing (Gemini, Claude, OpenAI, and local Ollama bridges)
- **DeepFind Research Agent:** Public domain intelligence gathering (DNS, WHOIS, HTTP headers, technology detection, Wayback archive)
- **Localization:** Bilingual architecture with instant toggle between English and Arabic (العربية) with full RTL layout direction support
- **Theme:** High-contrast OneShot copper/orange aesthetic (`#E05520`) with dynamic Dark and Light modes

## Key Features

1. **Chat & Model Router:** Multi-turn conversational intelligence with instant model switching, document attachments, voice notes, and live speech listening.
2. **Project Workspaces:** Isolated project configurations with dedicated system instructions and model routing.
3. **Specialized AI Agents:**
   - General Manager (Orchestrator)
   - Email Agent (Communications)
   - DeepFind Research Agent (Domain Intelligence & Evidence Gathering)
   - Developer Agent (Engineering)
   - Operations Agent (Invoicing & Telemetry)
   - Marketing Agent (Content & Analytics)
4. **Autonomous Tasks:** Immediate, scheduled, recurring, and watch jobs with active/paused toggle controls.
5. **Company Memory:** Centralized knowledge base categorized across Brand, Company, Engineering, and Operations.
6. **Tool Integrations:** Cloud productivity connectors (Gmail, Calendar, Drive, GitHub, Slack) and local inference bridges.

## Building and Running

The project is configured with Gradle (Kotlin DSL):
- Root: `settings.gradle.kts` and `build.gradle.kts`
- Version Catalog: `gradle/libs.versions.toml`
- App Module: `app/build.gradle.kts`
- Entry Point: `com.example.oneshotai.MainActivity`
