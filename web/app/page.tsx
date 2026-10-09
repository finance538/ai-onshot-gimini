"use client";
import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type FormEvent,
} from "react";
import {
  getUser,
  handleAuthCallback,
  logout,
  onAuthChange,
  type User,
} from "@netlify/identity";
import {
  assistants,
  defaultModel,
  findAssistant,
  models,
  type Message,
  type ServiceStatus,
  type WorkspaceItem,
} from "../lib/catalog";
import { api, copyText, download, newItem } from "../lib/client";
import { utilities } from "../lib/utilities";
import { Icon } from "../components/icon";
import { Markdown } from "../components/markdown";
import { ToolLibrary } from "../components/tools";
import { Records } from "../components/records";
import { AuthDialog, type AuthMode } from "../components/auth";

type View =
  | "chat"
  | "tools"
  | "agents"
  | "projects"
  | "tasks"
  | "knowledge"
  | "settings"
  | "history";
export default function Home() {
  const [lang, setLang] = useState<"en" | "ar">("en");
  const [theme, setTheme] = useState<"dark" | "light">("dark");
  const [view, setView] = useState<View>("chat");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const userId = useRef<string | null>(null);
  const [auth, setAuth] = useState<AuthMode | null>(null);
  const [inviteToken, setInviteToken] = useState("");
  const [items, setItems] = useState<WorkspaceItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [saveStatus, setSaveStatus] = useState({ id: "", value: "" });
  const [accountRevision, setAccountRevision] = useState(0);
  const [nextOffset, setNextOffset] = useState<number | null>(null);
  const [chat, setChat] = useState<WorkspaceItem | null>(null);
  const saveState = saveStatus.id === chat?.id ? saveStatus.value : "";
  function setSaveState(value: string, id = chat?.id || "") {
    setSaveStatus({ id, value });
  }
  const [context, setContext] = useState<{
    title: string;
    body: string;
  } | null>(null);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const controller = useRef<AbortController | null>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [statusError, setStatusError] = useState("");
  const [status, setStatus] = useState<ServiceStatus | null>(null);
  const [online, setOnline] = useState(true);
  const [noteDraft, setNoteDraft] = useState<WorkspaceItem | null>(null);
  const [legacy, setLegacy] = useState<unknown[]>([]);
  const [historySearch, setHistorySearch] = useState("");
  const [deleteId, setDeleteId] = useState("");
  const messageEnd = useRef<HTMLDivElement>(null);
  const composer = useRef<HTMLTextAreaElement>(null);
  const attachment = useRef<HTMLInputElement>(null);
  const started = useRef(false);
  const ar = lang === "ar";
  const t = (en: string, arabic: string) => (ar ? arabic : en);
  const assistant = findAssistant(chat?.agent || "general");
  const nav: { id: View; icon: string; en: string; ar: string }[] = [
    { id: "chat", icon: "chat", en: "Conversation", ar: "المحادثة" },
    { id: "tools", icon: "tools", en: "Tool library", ar: "مكتبة الأدوات" },
    { id: "agents", icon: "bot", en: "AI assistants", ar: "المساعدون" },
    { id: "projects", icon: "folder", en: "Projects", ar: "المشاريع" },
    { id: "tasks", icon: "checks", en: "Tasks", ar: "المهام" },
    { id: "knowledge", icon: "book", en: "Knowledge", ar: "المعرفة" },
  ];

  const refreshStatus = useCallback(async () => {
    setStatusError("");
    try {
      setStatus(await api<ServiceStatus>("/api/status"));
    } catch {
      setStatusError("Connection status is unavailable. Try refreshing.");
    }
  }, []);
  const refreshItems = useCallback(async () => {
    const owner = userId.current;
    if (!owner) return;
    setLoading(true);
    try {
      const data = await api<{
        items: WorkspaceItem[];
        nextOffset: number | null;
      }>("/api/workspace");
      if (userId.current === owner) {
        setItems(data.items);
        setNextOffset(data.nextOffset);
      }
    } catch (err) {
      if (userId.current === owner) setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    if (started.current) return;
    started.current = true;
    setChat(newItem());
    setOnline(navigator.onLine);
    try {
      const savedLang = localStorage.getItem("oneshot-gimini:lang"),
        savedTheme = localStorage.getItem("oneshot-gimini:theme");
      if (savedLang === "en" || savedLang === "ar") setLang(savedLang);
      if (savedTheme === "dark" || savedTheme === "light") setTheme(savedTheme);
      const old: unknown = JSON.parse(
        localStorage.getItem("oneshot-gimini:chats") || "[]",
      );
      if (Array.isArray(old)) setLegacy(old);
    } catch {
      /* Browser storage may be disabled. Saving uses the server database. */
    }
    refreshStatus();
    (async () => {
      try {
        const callback = await handleAuthCallback();
        if (callback?.type === "recovery") setAuth("reset");
        if (callback?.type === "invite") {
          setInviteToken(callback.token || "");
          setAuth("invite");
        }
        const current = await getUser();
        userId.current = current?.id || null;
        setUser(current);
        if (current) refreshItems();
      } catch {
        setError(
          "Account confirmation could not be completed. Try signing in or request a new recovery link.",
        );
      }
    })();
  }, [refreshItems, refreshStatus]);
  useEffect(() => {
    const unsubscribe = onAuthChange((event, next) => {
      const previousId = userId.current;
      const changed = userId.current !== (next?.id || null);
      userId.current = next?.id || null;
      setUser(next);
      if (event === "logout" || (changed && previousId)) {
        setAccountRevision((revision) => revision + 1);
        controller.current?.abort();
        setItems([]);
        setChat(newItem());
        setContext(null);
        setSaveState("");
        setInput("");
        setNoteDraft(null);
      }
      if (next && changed) {
        setItems([]);
        refreshItems();
      }
    });
    const connect = () => setOnline(navigator.onLine);
    window.addEventListener("online", connect);
    window.addEventListener("offline", connect);
    return () => {
      unsubscribe();
      window.removeEventListener("online", connect);
      window.removeEventListener("offline", connect);
    };
  }, [refreshItems]);
  useEffect(() => {
    document.documentElement.dir = ar ? "rtl" : "ltr";
    document.documentElement.lang = lang;
    document.documentElement.dataset.theme = theme;
  }, [ar, lang, theme]);
  useEffect(() => {
    messageEnd.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [chat?.messages.length, busy]);
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(""), 4000);
    return () => clearTimeout(timer);
  }, [notice]);
  useEffect(() => {
    const warn = (e: BeforeUnloadEvent) => {
      if (
        input.trim() ||
        busy ||
        (chat?.messages.length && saveState !== "saved")
      ) {
        e.preventDefault();
      }
    };
    window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [input, busy, chat?.messages.length, saveState]);

  function navigate(next: View) {
    setView(next);
    setMobileMenu(false);
    setError("");
  }
  async function loadMore() {
    if (nextOffset === null || loading) return;
    const owner = userId.current;
    setLoading(true);
    try {
      const data = await api<{
        items: WorkspaceItem[];
        nextOffset: number | null;
      }>(`/api/workspace?offset=${nextOffset}`);
      if (userId.current === owner) {
        setItems((previous) => [
          ...previous,
          ...data.items.filter(
            (item) => !previous.some((old) => old.id === item.id),
          ),
        ]);
        setNextOffset(data.nextOffset);
      }
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }
  function startChat(agent = "general") {
    controller.current?.abort();
    controller.current = null;
    setBusy(false);
    setChat({ ...newItem("chat", agent), model: chat?.model || defaultModel });
    setInput("");
    setContext(null);
    setSaveState("");
    navigate("chat");
  }
  async function saveItem(item: WorkspaceItem) {
    const owner = userId.current;
    if (!owner)
      throw new Error(
        t("Sign in to save your work.", "سجّل الدخول لحفظ عملك."),
      );
    const result = await api<{ item: WorkspaceItem }>(
      "/api/workspace",
      {
        method: "PUT",
        body: JSON.stringify({
          ...item,
          messages: item.messages || [],
          contextId: item.contextId || "",
        }),
      },
      owner,
    );
    if (userId.current === owner)
      setItems((previous) => [
        result.item,
        ...previous.filter((i) => i.id !== result.item.id),
      ]);
  }
  async function saveConversation(item = chat) {
    if (!item?.messages.length) return;
    if (!userId.current) {
      setAuth("login");
      return;
    }
    setSaveState("saving", item.id);
    try {
      await saveItem(item);
      setSaveState("saved", item.id);
    } catch (err) {
      setSaveState("failed", item.id);
      setError((err as Error).message);
    }
  }
  async function deleteItem(id: string) {
    await api("/api/workspace?id=" + encodeURIComponent(id), {
      method: "DELETE",
    });
    setItems((previous) => previous.filter((i) => i.id !== id));
    if (chat?.id === id) startChat();
  }
  async function openChat(id: string) {
    controller.current?.abort();
    controller.current = null;
    setBusy(false);
    setError("");
    setLoading(true);
    const owner = userId.current;
    try {
      const data = await api<{ item: WorkspaceItem }>(
        "/api/workspace?id=" + encodeURIComponent(id),
      );
      if (userId.current !== owner) return;
      setChat(data.item);
      setContext(
        data.item.body
          ? { title: t("Saved reference", "مرجع محفوظ"), body: data.item.body }
          : null,
      );
      setInput("");
      setSaveState("saved", data.item.id);
      navigate("chat");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }
  async function requestReply(next: WorkspaceItem) {
    if (controller.current || !online) return;
    const abort = new AbortController();
    controller.current = abort;
    const owner = userId.current;
    setChat(next);
    setBusy(true);
    setError("");
    setSaveState("", next.id);
    try {
      const isHermes = next.agent === "hermes";
      const result = await api<{ text: string; contextId?: string }>(
        isHermes ? "/api/hermes" : "/api/chat",
        {
          method: "POST",
          signal: abort.signal,
          body: JSON.stringify(
            isHermes
              ? {
                  message: next.messages.at(-1)?.content,
                  contextId: next.contextId || undefined,
                }
              : {
                  messages: next.messages,
                  model: next.model,
                  agent: next.agent,
                  context: context?.body || next.body,
                },
          ),
        },
      );
      if (abort.signal.aborted || userId.current !== owner) return;
      if (typeof result.text !== "string" || !result.text.trim())
        throw new Error("The model returned no text. Please try again.");
      const completed = {
        ...next,
        messages: [
          ...next.messages,
          {
            role: "assistant" as const,
            content: result.text,
            model: next.agent === "hermes" ? "hermes" : next.model,
          },
        ],
        contextId: result.contextId || next.contextId,
        body: context?.body || next.body,
      };
      setChat((current) => (current?.id === next.id ? completed : current));
      if (owner) {
        setSaveState("saving", next.id);
        try {
          await saveItem(completed);
          setSaveState("saved", next.id);
        } catch (err) {
          setSaveState("failed", next.id);
          setError((err as Error).message);
        }
      }
    } catch (err) {
      if (!abort.signal.aborted) setError((err as Error).message);
    } finally {
      if (controller.current === abort) {
        controller.current = null;
        setBusy(false);
      }
    }
  }
  function send(e: FormEvent) {
    e.preventDefault();
    if (!chat || busy || controller.current || !online || !input.trim()) return;
    if (chat.messages.length >= 79) {
      setError(
        t(
          "This conversation has reached its message limit. Export it and start a new conversation.",
          "وصلت المحادثة إلى حد الرسائل. نزّلها وابدأ محادثة جديدة.",
        ),
      );
      return;
    }
    const text = input.trim();
    const next = {
      ...chat,
      title: chat.messages.length ? chat.title : text.slice(0, 80),
      messages: [...chat.messages, { role: "user" as const, content: text }],
      body: context?.body || chat.body,
    };
    setInput("");
    requestReply(next);
  }
  function stop() {
    controller.current?.abort();
    controller.current = null;
    setBusy(false);
    setNotice(
      t(
        "Generation stopped. You can retry this message.",
        "توقف التوليد. يمكنك إعادة محاولة الرسالة.",
      ),
    );
  }
  function useReference(item: WorkspaceItem) {
    startChat();
    setContext({ title: item.title, body: item.body });
  }
  function saveNote(text: string, title: string) {
    if (text.length > 12000) {
      setNotice(
        t(
          "This result exceeds the 12,000-character note limit. Download it or shorten it before saving.",
          "تتجاوز النتيجة حد الملاحظة البالغ 12000 حرف. نزّلها أو اختصرها قبل الحفظ.",
        ),
      );
      return;
    }
    setNoteDraft({
      ...newItem("note"),
      title: title.slice(0, 120),
      body: text.slice(0, 12000),
    });
    navigate("knowledge");
  }
  async function copy(text: string) {
    try {
      await copyText(text);
      setNotice(t("Copied", "تم النسخ"));
    } catch (err) {
      setError((err as Error).message);
    }
  }
  async function attach(file?: File) {
    if (!file) return;
    if (
      file.size > 60000 ||
      !/\.(txt|md|csv|json|js|jsx|ts|tsx|py|html|css|yml|yaml|xml|log)$/i.test(
        file.name,
      )
    ) {
      setError(
        t(
          "Choose a text, code, Markdown, JSON, or CSV file under 60 KB.",
          "اختر ملفاً نصياً أو برمجياً أو Markdown أو JSON أو CSV أقل من 60 كيلوبايت.",
        ),
      );
      return;
    }
    const text = await file.text();
    const combined = `${input}${input ? "\n\n" : ""}File: ${file.name}\n${text}`;
    if (combined.length > 24000) {
      setError(
        t(
          "The message with this file exceeds 24,000 characters.",
          "الرسالة مع هذا الملف تتجاوز 24000 حرف.",
        ),
      );
      return;
    }
    setInput(combined);
    setError("");
    composer.current?.focus();
  }
  async function importLegacy() {
    if (!user) {
      setAuth("login");
      return;
    }
    setLoading(true);
    setError("");
    let count = 0;
    try {
      for (const value of legacy) {
        if (!value || typeof value !== "object") continue;
        const old = value as Record<string, unknown>;
        if (!Array.isArray(old.messages) || !old.messages.length) continue;
        const id =
          typeof old.id === "string" && /^[0-9a-f-]{36}$/i.test(old.id)
            ? old.id
            : crypto.randomUUID();
        await saveItem({
          ...newItem(),
          id,
          title:
            typeof old.title === "string"
              ? old.title.slice(0, 120)
              : "Imported conversation",
          agent: old.agent === "hermes" ? "hermes" : "general",
          messages: old.messages as Message[],
        });
        count++;
      }
      setLegacy([]);
      setNotice(
        `${count} ${t("conversations imported. Your original browser copy is preserved.", "محادثات مستوردة. تم الاحتفاظ بنسخة المتصفح الأصلية.")}`,
      );
    } catch (err) {
      setError(`${count} imported. ${(err as Error).message}`);
    } finally {
      setLoading(false);
    }
  }
  function exportChat() {
    if (chat)
      download(
        "oneshot-conversation.md",
        `# ${chat.title}\n\n${chat.messages.map((m) => `## ${m.role === "user" ? "You" : "OneShot AI"}\n\n${m.content}`).join("\n\n")}`,
        "text/markdown;charset=utf-8",
      );
  }
  const history = items.filter((i) => i.kind === "chat");

  return (
    <main className="app">
      <a className="skip-link" href="#main-content">
        {t("Skip to content", "تخطَّ إلى المحتوى")}
      </a>
      {mobileMenu && (
        <button
          className="sidebar-overlay"
          aria-label={t("Close navigation", "إغلاق القائمة")}
          onClick={() => setMobileMenu(false)}
        />
      )}
      <aside className={`sidebar ${mobileMenu ? "is-open" : ""}`}>
        <button className="brand" onClick={() => navigate("chat")}>
          <img src="/oneshot.svg" alt="" width={35} height={35} />
          <span>
            OneShot<span className="brand-ai"> AI</span>
          </span>
          <span className="brand-dot" />
        </button>
        <button className="new-chat" onClick={() => startChat()}>
          <Icon name="plus" size={19} />
          {t("New conversation", "محادثة جديدة")}
          <span>↗</span>
        </button>
        <div className="nav-label">{t("WORKSPACE", "مساحة العمل")}</div>
        <nav aria-label={t("Main navigation", "القائمة الرئيسية")}>
          {nav.map((n) => (
            <button
              key={n.id}
              className={view === n.id ? "nav-item active" : "nav-item"}
              onClick={() => navigate(n.id)}
            >
              <Icon name={n.icon} />
              <span>{ar ? n.ar : n.en}</span>
              {n.id === "tools" && (
                <small>{utilities.length + assistants.length}</small>
              )}
              {n.id === "tasks" &&
                items.some((i) => i.kind === "task" && !i.completed) && (
                  <small>
                    {
                      items.filter((i) => i.kind === "task" && !i.completed)
                        .length
                    }
                  </small>
                )}
            </button>
          ))}
        </nav>
        <div className="history-label">
          <span className="nav-label">
            {t("RECENT CONVERSATIONS", "المحادثات الأخيرة")}
          </span>
          <button
            className="icon-button"
            aria-label={t("All conversations", "كل المحادثات")}
            onClick={() => navigate("history")}
          >
            <Icon name="more" size={17} />
          </button>
        </div>
        <div className="recent-list">
          {history.slice(0, 5).map((item) => (
            <button
              key={item.id}
              onClick={() => openChat(item.id)}
              title={item.title}
            >
              <Icon name="chat" size={14} />
              <span>{item.title}</span>
            </button>
          ))}
          {!history.length && (
            <p>
              {user
                ? t(
                    "Your saved conversations appear here.",
                    "تظهر محادثاتك المحفوظة هنا.",
                  )
                : t(
                    "Sign in to keep your conversations.",
                    "سجّل الدخول للاحتفاظ بمحادثاتك.",
                  )}
            </p>
          )}
        </div>
        <div className="sidebar-bottom">
          <button className="workspace-note" onClick={() => navigate("tools")}>
            <span className="note-symbol">
              <Icon name="sparkles" size={18} />
            </span>
            <strong>
              {t(
                "One space. More possibilities.",
                "مساحة واحدة. إمكانات أكثر.",
              )}
            </strong>
            <span>
              {t("Explore your full toolkit", "استكشف كل أدواتك")}
              <Icon name="arrow" size={15} />
            </span>
          </button>
          <button
            className={`nav-item ${view === "settings" ? "active" : ""}`}
            onClick={() => navigate("settings")}
          >
            <Icon name="settings" />
            <span>{t("Settings & connections", "الإعدادات والاتصالات")}</span>
          </button>
          <button
            className="account-button"
            onClick={() => (user ? navigate("settings") : setAuth("login"))}
          >
            <span className="avatar">
              {user ? (
                (user.name || user.email || "U").slice(0, 1).toUpperCase()
              ) : (
                <Icon name="user" size={18} />
              )}
            </span>
            <span>
              <strong>
                {user?.name ||
                  (user
                    ? t("My account", "حسابي")
                    : t("Personal workspace", "مساحة شخصية"))}
              </strong>
              <small>
                {user
                  ? t("Signed in", "تم تسجيل الدخول")
                  : t("Sign in to save your work", "سجّل الدخول لحفظ عملك")}
              </small>
            </span>
            <Icon name={user ? "chevron" : "login"} size={16} />
          </button>
        </div>
      </aside>
      <section className="main">
        <header className="topbar">
          <div className="breadcrumb">
            <button
              className="icon-button mobile-menu"
              aria-label={t("Open navigation", "فتح القائمة")}
              onClick={() => setMobileMenu(true)}
            >
              <Icon name="menu" />
            </button>
            <span className="breadcrumb-brand">OneShot AI</span>
            <span className="breadcrumb-slash">/</span>
            <span>
              {nav.find((n) => n.id === view)?.[ar ? "ar" : "en"] ||
                (view === "history"
                  ? t("Conversations", "المحادثات")
                  : t("Settings", "الإعدادات"))}
            </span>
          </div>
          <div className="top-actions">
            <span className="workspace-badge">
              <span className={`status-dot ${!online ? "offline" : ""}`} />
              {online
                ? t("Your space to create", "مساحتك للإبداع")
                : t("Offline", "غير متصل")}
            </span>
            <button
              className="icon-button language-toggle"
              aria-label={ar ? "Switch to English" : "التبديل إلى العربية"}
              onClick={() => {
                const next = ar ? "en" : "ar";
                setLang(next);
                try {
                  localStorage.setItem("oneshot-gimini:lang", next);
                } catch {}
              }}
            >
              {ar ? "EN" : "ع"}
            </button>
            <button
              className="icon-button"
              aria-label={t("Toggle color theme", "تبديل المظهر")}
              onClick={() => {
                const next = theme === "dark" ? "light" : "dark";
                setTheme(next);
                try {
                  localStorage.setItem("oneshot-gimini:theme", next);
                } catch {}
              }}
            >
              <Icon name={theme === "dark" ? "sun" : "moon"} size={18} />
            </button>
          </div>
        </header>
        {!online && (
          <div className="offline-banner" role="status">
            {t(
              "You’re offline. Browser tools still work on this page. AI and saved work need a connection.",
              "أنت غير متصل. لا تزال أدوات المتصفح تعمل هنا. يتطلب الذكاء الاصطناعي والعمل المحفوظ اتصالاً.",
            )}
          </div>
        )}
        {notice && (
          <div className="toast" role="status">
            <Icon name="check" size={17} />
            {notice}
          </div>
        )}
        <div id="main-content" className="main-content" tabIndex={-1}>
          {view === "chat" ? (
            <div className="chat-view">
              <div className="chat-scroll">
                {!chat?.messages.length ? (
                  <div className="welcome enter">
                    <div className="welcome-top">
                      <span className="eyebrow">
                        <span className="status-dot" />
                        {t("A GOOD PLACE TO START", "مكان مناسب للبداية")}
                      </span>
                      <span className="edition">WORKSPACE / 01</span>
                    </div>
                    <div className="welcome-heading">
                      <div>
                        <h1>
                          {chat?.agent === "hermes" ? (
                            <>
                              OneShot–<em>Hermes.</em>
                            </>
                          ) : chat?.agent !== "general" ? (
                            <>
                              {ar ? assistant?.ar : assistant?.name}
                              <em>{t("Let’s get to it.", "لنبدأ.")}</em>
                            </>
                          ) : (
                            <>
                              {t("Your ideas,", "أفكارك،")}
                              <em>{t("into motion.", "تتحول إلى واقع.")}</em>
                            </>
                          )}
                        </h1>
                        <p>
                          {chat?.agent !== "general"
                            ? assistant?.description ||
                              t(
                                "Send a task to your connected Hermes agent.",
                                "أرسل مهمة إلى وكيل Hermes المتصل.",
                              )
                            : t(
                                "A thought partner, a fresh perspective, and the right tools. What are we making today?",
                                "شريك للتفكير، منظور جديد، والأدوات المناسبة. ماذا نبتكر اليوم؟",
                              )}
                        </p>
                      </div>
                      <div className="orbital-mark" aria-hidden="true">
                        <span />
                        <span />
                        <span />
                        <i />
                        <b>1</b>
                      </div>
                    </div>
                    <div className="start-label">
                      <span>
                        {t("PICK A STARTING POINT", "اختر نقطة البداية")}
                      </span>
                      <span className="tiny-line" />
                    </div>
                    <div className="starter-grid">
                      {[
                        {
                          id: "writer",
                          icon: "pen",
                          en: "Find the right words",
                          ar: "اعثر على الكلمات المناسبة",
                          sub: "Draft, rewrite, or tell a story",
                          prompt:
                            "Help me write a clear first draft. Ask me about the topic and audience.",
                        },
                        {
                          id: "developer",
                          icon: "code",
                          en: "Build something useful",
                          ar: "ابنِ شيئاً مفيداً",
                          sub: "Code, debug, and solve a problem",
                          prompt:
                            "Help me plan a small software project. Ask about what I want to build.",
                        },
                        {
                          id: "learn",
                          icon: "book",
                          en: "Follow your curiosity",
                          ar: "اتبع فضولك",
                          sub: "Understand something new",
                          prompt:
                            "Teach me a topic step by step. Ask what I want to learn and my experience level.",
                        },
                        {
                          id: "gm",
                          icon: "folder",
                          en: "Make a plan that sticks",
                          ar: "ضع خطة قابلة للتنفيذ",
                          sub: "Turn a big goal into next steps",
                          prompt:
                            "Help me turn a goal into a practical plan. Ask what I want to achieve.",
                        },
                      ].map((s, i) => (
                        <button
                          key={s.id}
                          className="starter"
                          onClick={() => {
                            if (chat) setChat({ ...chat, agent: s.id });
                            setInput(s.prompt);
                            composer.current?.focus();
                          }}
                        >
                          <span className="starter-icon">
                            <Icon name={s.icon} />
                          </span>
                          <span>
                            <strong>{ar ? s.ar : s.en}</strong>
                            <small>
                              {ar
                                ? t("ابدأ بمساعد متخصص", "ابدأ بمساعد متخصص")
                                : s.sub}
                            </small>
                          </span>
                          <span className="starter-number">0{i + 1}</span>
                        </button>
                      ))}
                    </div>
                    <button
                      className="explore-link"
                      onClick={() => navigate("tools")}
                    >
                      <span>
                        <Icon name="tools" size={16} />
                        {t("Need a specific tool?", "تحتاج أداة محددة؟")}
                      </span>
                      <strong>
                        {t(
                          `Explore all ${utilities.length + assistants.length} tools`,
                          `استكشف ${utilities.length + assistants.length} أداة`,
                        )}
                        <Icon name="arrow" size={16} />
                      </strong>
                    </button>
                  </div>
                ) : (
                  <div className="messages">
                    <div className="conversation-heading">
                      <span>{chat.title}</span>
                      <div>
                        <button
                          className="icon-button"
                          aria-label={t(
                            "Export conversation",
                            "تنزيل المحادثة",
                          )}
                          onClick={exportChat}
                        >
                          <Icon name="download" size={17} />
                        </button>
                        <button
                          className="icon-button"
                          aria-label={t("Save conversation", "حفظ المحادثة")}
                          disabled={busy || saveState === "saving"}
                          onClick={() => saveConversation()}
                        >
                          <Icon name="database" size={17} />
                        </button>
                      </div>
                    </div>
                    {chat.messages.map((m, index) => (
                      <article className={`message ${m.role}`} key={index}>
                        <div className="message-avatar">
                          {m.role === "user" ? (
                            <Icon name="user" size={17} />
                          ) : (
                            <img
                              src="/oneshot.svg"
                              alt=""
                              width={24}
                              height={24}
                            />
                          )}
                        </div>
                        <div className="message-body">
                          <div className="message-author">
                            {m.role === "user" ? t("You", "أنت") : "OneShot AI"}
                            {m.role === "assistant" && (
                              <span>
                                {m.model === "hermes"
                                  ? "Hermes"
                                  : models.find((model) => model.id === m.model)
                                      ?.name}
                              </span>
                            )}
                          </div>
                          {m.role === "user" ? (
                            <div className="user-text" dir="auto">
                              {m.content}
                            </div>
                          ) : (
                            <Markdown text={m.content} />
                          )}
                          {m.role === "assistant" && (
                            <div className="message-actions">
                              <button
                                className="text-button"
                                onClick={() => copy(m.content)}
                              >
                                <Icon name="copy" size={14} />
                                {t("Copy", "نسخ")}
                              </button>
                              <button
                                className="text-button"
                                onClick={() => saveNote(m.content, chat.title)}
                              >
                                <Icon name="book" size={14} />
                                {t("Save note", "حفظ ملاحظة")}
                              </button>
                              {index === chat.messages.length - 1 && (
                                <button
                                  className="text-button"
                                  disabled={busy}
                                  onClick={() =>
                                    requestReply({
                                      ...chat,
                                      messages: chat.messages.slice(0, -1),
                                    })
                                  }
                                >
                                  <Icon name="refresh" size={14} />
                                  {t("Regenerate", "إعادة التوليد")}
                                </button>
                              )}
                            </div>
                          )}
                        </div>
                      </article>
                    ))}
                    {busy && (
                      <div className="thinking" role="status">
                        <img src="/oneshot.svg" alt="" width={22} height={22} />
                        <span>
                          {t("Thinking it through", "جارٍ التفكير")}
                          <i />
                          <i />
                          <i />
                        </span>
                      </div>
                    )}
                    <div ref={messageEnd} />
                  </div>
                )}
              </div>
              <div className="composer-area">
                {error && (
                  <div className="error" role="alert">
                    {error}
                    {chat?.messages.at(-1)?.role === "user" && !busy && (
                      <button
                        className="text-button"
                        onClick={() => requestReply(chat)}
                      >
                        {t("Retry", "إعادة المحاولة")}
                      </button>
                    )}
                    {saveState === "failed" && (
                      <button
                        className="text-button"
                        onClick={() => saveConversation()}
                      >
                        {t("Retry saving", "إعادة الحفظ")}
                      </button>
                    )}
                  </div>
                )}
                {context && (
                  <div className="context-strip">
                    <Icon name="book" size={15} />
                    <span>
                      {t("Reference", "مرجع")}: {context.title}
                    </span>
                    <button
                      className="icon-button"
                      aria-label={t("Remove reference", "إزالة المرجع")}
                      onClick={() => {
                        setContext(null);
                        if (chat) setChat({ ...chat, body: "" });
                      }}
                    >
                      <Icon name="close" size={14} />
                    </button>
                  </div>
                )}
                <form className="composer" onSubmit={send}>
                  <textarea
                    ref={composer}
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    maxLength={24000}
                    aria-label={t("Message OneShot AI", "رسالة إلى OneShot AI")}
                    placeholder={t(
                      "Bring an idea. Ask a question. Make a start…",
                      "شارك فكرة. اطرح سؤالاً. ابدأ الآن…",
                    )}
                    dir="auto"
                    onKeyDown={(e) => {
                      if (
                        e.key === "Enter" &&
                        !e.shiftKey &&
                        !e.nativeEvent.isComposing &&
                        window.matchMedia("(min-width: 821px)").matches
                      ) {
                        e.preventDefault();
                        e.currentTarget.form?.requestSubmit();
                      }
                    }}
                  />
                  <div className="composer-toolbar">
                    <div className="composer-options">
                      <input
                        ref={attachment}
                        hidden
                        type="file"
                        accept=".txt,.md,.csv,.json,.js,.jsx,.ts,.tsx,.py,.html,.css,.yml,.yaml,.xml,.log"
                        onChange={(e) => {
                          attach(e.target.files?.[0]).catch(() =>
                            setError("Could not read this file."),
                          );
                          e.target.value = "";
                        }}
                      />
                      <button
                        type="button"
                        className="icon-button"
                        title={t("Attach a text file", "إرفاق ملف نصي")}
                        aria-label={t("Attach a text file", "إرفاق ملف نصي")}
                        onClick={() => attachment.current?.click()}
                      >
                        <Icon name="attachment" size={18} />
                      </button>
                      <span className="toolbar-divider" />
                      <label className="model-picker">
                        <Icon name="sparkles" size={15} />
                        <select
                          aria-label={t("AI model", "نموذج الذكاء الاصطناعي")}
                          value={chat?.model || defaultModel}
                          disabled={busy || chat?.agent === "hermes"}
                          onChange={(e) => {
                            if (chat)
                              setChat({ ...chat, model: e.target.value });
                          }}
                        >
                          {["gemini", "openai", "anthropic"].map((provider) => (
                            <optgroup
                              key={provider}
                              label={
                                provider === "gemini"
                                  ? "Google Gemini"
                                  : provider === "openai"
                                    ? "OpenAI"
                                    : "Anthropic"
                              }
                            >
                              {models
                                .filter((m) => m.provider === provider)
                                .map((m) => (
                                  <option
                                    key={m.id}
                                    value={m.id}
                                    disabled={
                                      status?.providers[m.provider] === false
                                    }
                                  >
                                    {m.name}
                                    {status?.providers[m.provider] === false
                                      ? " · unavailable"
                                      : ""}
                                  </option>
                                ))}
                            </optgroup>
                          ))}
                        </select>
                      </label>
                    </div>
                    {busy ? (
                      <button
                        type="button"
                        className="send-button"
                        aria-label={t("Stop generation", "إيقاف التوليد")}
                        onClick={stop}
                      >
                        <Icon name="stop" size={17} />
                      </button>
                    ) : (
                      <button
                        className="send-button"
                        aria-label={t("Send message", "إرسال الرسالة")}
                        disabled={
                          !input.trim() ||
                          !chat ||
                          !online ||
                          (chat.agent === "hermes" && !status?.hermes)
                        }
                      >
                        <Icon name="up" size={20} />
                      </button>
                    )}
                  </div>
                </form>
                <div className="composer-footnote">
                  <span>
                    {t(
                      "AI can make mistakes. Check important details.",
                      "قد يخطئ الذكاء الاصطناعي. تحقق من التفاصيل المهمة.",
                    )}
                  </span>
                  <span>
                    {saveState === "saved"
                      ? t("Saved privately", "محفوظ بشكل خاص")
                      : saveState === "saving"
                        ? t("Saving…", "جارٍ الحفظ…")
                        : user
                          ? t("Saves after each reply", "يُحفظ بعد كل رد")
                          : t(
                              "Guest chat · not saved",
                              "محادثة ضيف · غير محفوظة",
                            )}
                  </span>
                </div>
                {!busy && chat?.messages.at(-1)?.role === "user" && !error && (
                  <button
                    className="text-button retry-link"
                    onClick={() => requestReply(chat)}
                  >
                    <Icon name="refresh" size={14} />
                    {t("Retry last message", "إعادة محاولة آخر رسالة")}
                  </button>
                )}
              </div>
            </div>
          ) : view === "tools" || view === "agents" ? (
            <ToolLibrary
              key={view}
              ar={ar}
              onlyAssistants={view === "agents"}
              hermes={status?.hermes}
              onAssistant={startChat}
              onSaveNote={saveNote}
            />
          ) : ["projects", "tasks", "knowledge"].includes(view) ? (
            <>
              <Records
                key={`${accountRevision}-${view}-${noteDraft?.id || ""}`}
                kind={
                  view === "projects"
                    ? "project"
                    : view === "tasks"
                      ? "task"
                      : "note"
                }
                items={items}
                signedIn={Boolean(user)}
                loading={loading}
                ar={ar}
                initialDraft={view === "knowledge" ? noteDraft : null}
                onSave={async (item) => {
                  await saveItem(item);
                  if (item.id === noteDraft?.id) setNoteDraft(null);
                }}
                onDelete={deleteItem}
                onSignIn={() => setAuth("login")}
                onUse={useReference}
                onRefresh={refreshItems}
              />
              {error && (
                <div className="error floating-error" role="alert">
                  {error}
                </div>
              )}
            </>
          ) : view === "history" ? (
            <div className="workspace enter">
              <div className="eyebrow">{t("YOUR WORKSPACE", "مساحة عملك")}</div>
              <div className="page-heading">
                <div>
                  <h1>{t("Conversations", "المحادثات")}</h1>
                  <p>
                    {t(
                      "Pick up where a good idea left off.",
                      "تابع من حيث توقفت فكرتك.",
                    )}
                  </p>
                </div>
                <button className="primary" onClick={() => startChat()}>
                  <Icon name="plus" />
                  {t("New chat", "محادثة جديدة")}
                </button>
              </div>
              <label className="search-field">
                <Icon name="search" />
                <input
                  aria-label={t("Search conversations", "البحث في المحادثات")}
                  placeholder={t("Search conversations…", "ابحث في المحادثات…")}
                  value={historySearch}
                  onChange={(e) => setHistorySearch(e.target.value)}
                />
              </label>
              {error && (
                <div className="error" role="alert">
                  {error}
                </div>
              )}
              {history
                .filter((c) =>
                  c.title.toLowerCase().includes(historySearch.toLowerCase()),
                )
                .map((c) => (
                  <div className="history-row" key={c.id}>
                    <button onClick={() => openChat(c.id)}>
                      <Icon name="chat" />
                      <span>{c.title}</span>
                    </button>
                    {deleteId === c.id ? (
                      <>
                        <button
                          className="danger-button"
                          onClick={() =>
                            deleteItem(c.id)
                              .then(() => setDeleteId(""))
                              .catch((e) => setError(e.message))
                          }
                        >
                          {t("Delete permanently", "حذف نهائي")}
                        </button>
                        <button
                          className="text-button"
                          onClick={() => setDeleteId("")}
                        >
                          {t("Cancel", "إلغاء")}
                        </button>
                      </>
                    ) : (
                      <button
                        className="icon-button"
                        aria-label={`${t("Delete", "حذف")} ${c.title}`}
                        onClick={() => setDeleteId(c.id)}
                      >
                        <Icon name="trash" size={17} />
                      </button>
                    )}
                  </div>
                ))}
              {!history.length && (
                <div className="empty-state">
                  <Icon name="chat" size={32} />
                  <h3>
                    {t(
                      "Your next conversation starts here.",
                      "محادثتك القادمة تبدأ هنا.",
                    )}
                  </h3>
                  <p>
                    {user
                      ? t(
                          "Saved conversations appear here after your first reply.",
                          "تظهر المحادثات المحفوظة بعد أول رد.",
                        )
                      : t(
                          "Sign in to keep and revisit your conversations.",
                          "سجّل الدخول لحفظ محادثاتك والعودة إليها.",
                        )}
                  </p>
                  {!user && (
                    <button
                      className="primary"
                      onClick={() => setAuth("login")}
                    >
                      {t("Sign in", "تسجيل الدخول")}
                    </button>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="workspace settings-page enter">
              <div className="eyebrow">
                {t("MAKE IT YOURS", "اجعلها مساحتك")}
              </div>
              <div className="page-heading">
                <div>
                  <h1>{t("Settings & connections", "الإعدادات والاتصالات")}</h1>
                  <p>
                    {t(
                      "Your account, available models, and workspace connections.",
                      "حسابك ونماذجك المتاحة واتصالات مساحة عملك.",
                    )}
                  </p>
                </div>
              </div>
              {error && (
                <div className="error" role="alert">
                  {error}
                </div>
              )}
              <section className="settings-section">
                <h2>{t("Your account", "حسابك")}</h2>
                <div className="settings-row">
                  <div>
                    <strong>
                      {user?.email || t("Guest workspace", "مساحة ضيف")}
                    </strong>
                    <p>
                      {user
                        ? t(
                            "Your saved items are private to this account.",
                            "عناصرك المحفوظة خاصة بهذا الحساب.",
                          )
                        : t(
                            "Use tools and chat as a guest. Sign in to save work across devices.",
                            "استخدم الأدوات والمحادثة كضيف. سجّل الدخول لحفظ عملك عبر الأجهزة.",
                          )}
                    </p>
                  </div>
                  <button
                    className="secondary"
                    onClick={() =>
                      user
                        ? logout().catch(() =>
                            setError(
                              "Sign-out could not be completed. Please retry.",
                            ),
                          )
                        : setAuth("login")
                    }
                  >
                    <Icon name={user ? "logout" : "login"} size={17} />
                    {user
                      ? t("Sign out", "تسجيل الخروج")
                      : t("Sign in", "تسجيل الدخول")}
                  </button>
                </div>
                {legacy.length > 0 && (
                  <div className="settings-row">
                    <div>
                      <strong>
                        {t(
                          "Conversations from the previous app",
                          "محادثات النسخة السابقة",
                        )}
                      </strong>
                      <p>
                        {legacy.length}{" "}
                        {t(
                          "browser conversations found. Import them into your account or download a backup.",
                          "محادثات في المتصفح. استوردها إلى حسابك أو نزّل نسخة احتياطية.",
                        )}
                      </p>
                    </div>
                    <div className="button-row">
                      <button
                        className="secondary"
                        disabled={loading}
                        onClick={importLegacy}
                      >
                        {t("Import", "استيراد")}
                      </button>
                      <button
                        className="text-button"
                        onClick={() =>
                          download(
                            "oneshot-legacy-backup.json",
                            JSON.stringify(legacy, null, 2),
                            "application/json",
                          )
                        }
                      >
                        {t("Download backup", "تنزيل نسخة")}
                      </button>
                    </div>
                  </div>
                )}
              </section>
              <section className="settings-section">
                <div className="panel-heading">
                  <h2>{t("AI models", "نماذج الذكاء الاصطناعي")}</h2>
                  <button className="text-button" onClick={refreshStatus}>
                    <Icon name="refresh" size={15} />
                    {t("Refresh status", "تحديث الحالة")}
                  </button>
                </div>
                <p className="muted">
                  {t(
                    "Configured means credentials are present; model access also depends on provider availability and account credits. AI usage is billed through your configured provider or Netlify AI Gateway.",
                    "تعني الحالة المهيأة وجود إعدادات الاتصال. يعتمد توفر النماذج أيضاً على المزود ورصيد الحساب. تُحتسب التكلفة عبر المزود أو بوابة Netlify AI.",
                  )}
                </p>
                {statusError && (
                  <div className="error" role="alert">
                    {statusError}
                  </div>
                )}
                <div className="model-list">
                  {models.map((model) => (
                    <div className="model-row" key={model.id}>
                      <span className={`provider-symbol ${model.provider}`}>
                        {model.provider === "gemini"
                          ? "G"
                          : model.provider === "openai"
                            ? "O"
                            : "A"}
                      </span>
                      <div>
                        <strong>{model.name}</strong>
                        <small>{model.detail}</small>
                      </div>
                      <span
                        className={`connection-status ${status?.providers[model.provider] ? "connected" : ""}`}
                      >
                        {status
                          ? status.providers[model.provider]
                            ? t("Configured", "مهيأ")
                            : t("Not connected", "غير متصل")
                          : t("Checking…", "جارٍ التحقق…")}
                      </span>
                    </div>
                  ))}
                </div>
              </section>
              <section className="settings-section">
                <h2>{t("External connections", "الاتصالات الخارجية")}</h2>
                <div className="settings-row">
                  <div>
                    <strong>OneShot–Hermes A2A</strong>
                    <p>
                      {t(
                        "Uses the existing /api/hermes endpoint. Only available when a Hermes service is configured.",
                        "يستخدم المسار الحالي /api/hermes ويتوفر عند إعداد خدمة Hermes.",
                      )}
                    </p>
                  </div>
                  <span
                    className={`connection-status ${status?.hermes ? "connected" : ""}`}
                  >
                    {status?.hermes
                      ? t("Configured", "مهيأ")
                      : t("Not connected", "غير متصل")}
                  </span>
                </div>
                <p className="muted">
                  {t(
                    "Gmail, Calendar, Drive, GitHub, and Slack are not connected. Assistants can help draft work, but cannot send, publish, or modify those services.",
                    "خدمات Gmail وCalendar وDrive وGitHub وSlack غير متصلة. يستطيع المساعدون صياغة المحتوى دون إرساله أو نشره أو تعديل تلك الخدمات.",
                  )}
                </p>
              </section>
              <section className="settings-section">
                <h2>{t("Android & website", "أندرويد والموقع")}</h2>
                <p>
                  {t(
                    "The original /api/chat and /api/hermes paths are preserved for existing Android clients. Text, tools, and navigation adapt to mobile screens.",
                    "تم الحفاظ على مساري /api/chat و/api/hermes لتطبيقات أندرويد الحالية. تتكيف الأدوات والمحادثات مع شاشات الهاتف.",
                  )}
                </p>
                <p className="muted">
                  {t(
                    "ai.1shotcam.com currently points to a different Netlify project. This workspace does not redirect or change that domain.",
                    "يشير ai.1shotcam.com حالياً إلى مشروع Netlify آخر. لا تُغيّر هذه المساحة ذلك النطاق أو تعيد التوجيه إليه.",
                  )}
                </p>
              </section>
            </div>
          )}
          {user &&
            nextOffset !== null &&
            ["history", "projects", "tasks", "knowledge"].includes(view) && (
              <div className="load-more">
                <button
                  className="secondary"
                  disabled={loading}
                  onClick={loadMore}
                >
                  {loading
                    ? t("Loading…", "جارٍ التحميل…")
                    : t("Load older saved work", "تحميل الأعمال الأقدم")}
                </button>
              </div>
            )}
        </div>
      </section>
      {auth && (
        <AuthDialog
          ar={ar}
          initialMode={auth}
          inviteToken={inviteToken}
          onClose={() => {
            setAuth(null);
            setInviteToken("");
          }}
        />
      )}
    </main>
  );
}
