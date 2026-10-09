export type Provider = "gemini" | "openai" | "anthropic";
export type Model = {
  id: string;
  name: string;
  provider: Provider;
  detail: string;
};
// Only models supported by Netlify AI Gateway belong in this allowlist.
export const models: Model[] = [
  {
    id: "gemini-2.5-flash",
    name: "Gemini 2.5 Flash",
    provider: "gemini",
    detail: "Quick everyday work",
  },
  {
    id: "gemini-2.5-flash-lite",
    name: "Gemini 2.5 Flash Lite",
    provider: "gemini",
    detail: "Lightweight tasks",
  },
  {
    id: "gemini-2.5-pro",
    name: "Gemini 2.5 Pro",
    provider: "gemini",
    detail: "Complex reasoning",
  },
  {
    id: "gemini-3-flash-preview",
    name: "Gemini 3 Flash · Preview",
    provider: "gemini",
    detail: "Fast reasoning",
  },
  {
    id: "gpt-4.1-mini",
    name: "GPT-4.1 mini",
    provider: "openai",
    detail: "Fast, focused answers",
  },
  {
    id: "gpt-4.1",
    name: "GPT-4.1",
    provider: "openai",
    detail: "Writing and code",
  },
  {
    id: "gpt-5-mini",
    name: "GPT-5 mini",
    provider: "openai",
    detail: "Efficient reasoning",
  },
  {
    id: "gpt-5.2",
    name: "GPT-5.2",
    provider: "openai",
    detail: "Detailed problem solving",
  },
  {
    id: "claude-haiku-4-5",
    name: "Claude Haiku 4.5",
    provider: "anthropic",
    detail: "Quick drafts and analysis",
  },
  {
    id: "claude-sonnet-4-6",
    name: "Claude Sonnet 4.6",
    provider: "anthropic",
    detail: "Coding and thoughtful writing",
  },
  {
    id: "claude-opus-4-6",
    name: "Claude Opus 4.6",
    provider: "anthropic",
    detail: "Deep, complex work",
  },
];
export const defaultModel = models[0].id;
export const assistants = [
  {
    id: "general",
    name: "General assistant",
    ar: "المساعد العام",
    category: "Everyday",
    icon: "sparkles",
    description:
      "Think through a question, make a plan, or start something new.",
    prompt:
      "Help with the task. Be clear, practical, and honest about uncertainty.",
  },
  {
    id: "writer",
    name: "Writing studio",
    ar: "استوديو الكتابة",
    category: "Writing",
    icon: "pen",
    description: "Turn a rough idea into a polished first draft.",
    prompt:
      "Write clear, original prose in the requested voice. Preserve factual accuracy.",
  },
  {
    id: "summarize",
    name: "Summarizer",
    ar: "تلخيص النصوص",
    category: "Writing",
    icon: "align",
    description: "Pull key points and actions out of long text.",
    prompt:
      "Summarize supplied text faithfully, separating key points, decisions, and actions. Do not add facts absent from the source.",
  },
  {
    id: "translate",
    name: "Translator",
    ar: "المترجم",
    category: "Writing",
    icon: "languages",
    description: "Translate text while keeping its meaning and tone.",
    prompt:
      "Translate into the requested language, preserving meaning, tone, names, and formatting. Ask for the target language if missing.",
  },
  {
    id: "rewrite",
    name: "Rewrite & proofread",
    ar: "إعادة الصياغة والتدقيق",
    category: "Writing",
    icon: "pen",
    description: "Fix grammar, simplify wording, or change the tone.",
    prompt:
      "Edit and proofread supplied text. Preserve meaning. Provide the revision and a short note about material changes.",
  },
  {
    id: "email",
    name: "Email drafter",
    ar: "صياغة البريد",
    category: "Work",
    icon: "mail",
    description: "Draft a reply, outreach email, or follow-up.",
    prompt:
      "Draft emails with subject and body. You cannot access mailboxes or send emails.",
  },
  {
    id: "developer",
    name: "Code assistant",
    ar: "مساعد البرمجة",
    category: "Development",
    icon: "code",
    description: "Write code, understand an API, or review a solution.",
    prompt:
      "Develop correct, maintainable code and explain tradeoffs. You cannot run code or access repositories; never claim tests passed.",
  },
  {
    id: "debug",
    name: "Debug companion",
    ar: "مساعد تصحيح الأخطاء",
    category: "Development",
    icon: "bug",
    description: "Trace an error to its cause and work through a fix.",
    prompt:
      "Analyze supplied code and errors. Distinguish evidence from hypotheses. Propose a minimal fix and verification steps. Do not claim to execute code.",
  },
  {
    id: "research",
    name: "Research planner",
    ar: "مخطط البحث",
    category: "Learning",
    icon: "search",
    description: "Map a topic, compare ideas, and plan source checks.",
    prompt:
      "Plan research and analyze supplied sources. You have no live web access. Never invent citations or URLs, or claim web searches. Flag time-sensitive facts for verification.",
  },
  {
    id: "learn",
    name: "Learning coach",
    ar: "مدرب التعلم",
    category: "Learning",
    icon: "book",
    description: "Explore a concept with examples and practice.",
    prompt:
      "Teach with examples matched to the user’s level and useful practice questions. State assumptions and show mathematical steps.",
  },
  {
    id: "marketing",
    name: "Content planner",
    ar: "مخطط المحتوى",
    category: "Work",
    icon: "megaphone",
    description: "Develop campaign ideas, social posts, and content plans.",
    prompt:
      "Create specific marketing drafts and actionable plans. Do not invent analytics or customer quotes. You cannot publish posts.",
  },
  {
    id: "gm",
    name: "Project planner",
    ar: "مخطط المشاريع",
    category: "Work",
    icon: "folder",
    description: "Break a goal into milestones, risks, and next actions.",
    prompt:
      "Turn goals into plans with milestones, dependencies, risks, and next actions. Do not claim to assign work or change external systems.",
  },
  {
    id: "ops",
    name: "Meeting notes",
    ar: "ملاحظات الاجتماعات",
    category: "Work",
    icon: "check",
    description: "Turn a transcript into decisions and follow-up tasks.",
    prompt:
      "Organize the supplied transcript into a summary, decisions, open questions, and actions. Mark missing owners and dates as unspecified.",
  },
  {
    id: "extract",
    name: "Structured extraction",
    ar: "استخراج البيانات",
    category: "Development",
    icon: "braces",
    description: "Extract JSON or a table from supplied text.",
    prompt:
      "Extract structured data in the requested schema. Return valid JSON when requested. Use null for missing values; never invent data.",
  },
  {
    id: "resume",
    name: "Resume editor",
    ar: "محرر السيرة الذاتية",
    category: "Work",
    icon: "file",
    description: "Improve a resume or tailor a cover letter.",
    prompt:
      "Improve resumes using only experience supplied by the user. Never fabricate credentials, employers, metrics, or achievements.",
  },
] as const;
export type AssistantId = (typeof assistants)[number]["id"];
const aliases: Record<string, string> = {
  gemini: "general",
  dev: "developer",
  deepfind: "research",
};
export function findAssistant(id: string) {
  return assistants.find((a) => a.id === (aliases[id] || id));
}
export type Message = {
  role: "user" | "assistant";
  content: string;
  model?: string;
};
export type ItemKind = "chat" | "project" | "task" | "note";
export type WorkspaceItem = {
  id: string;
  kind: ItemKind;
  title: string;
  body: string;
  completed: boolean;
  dueDate: string;
  model: string;
  agent: string;
  messages: Message[];
  contextId: string;
  updatedAt: string;
};
export type ServiceStatus = {
  providers: Record<Provider, boolean>;
  hermes: boolean;
  models: Model[];
};
