"use client";
import { useState, type FormEvent } from "react";
import { newItem } from "../lib/client";
import type { ItemKind, WorkspaceItem } from "../lib/catalog";
import { Icon } from "./icon";
export function Records({
  kind,
  items,
  signedIn,
  loading,
  ar,
  initialDraft,
  onSave,
  onDelete,
  onSignIn,
  onUse,
  onRefresh,
}: {
  kind: Exclude<ItemKind, "chat">;
  items: WorkspaceItem[];
  signedIn: boolean;
  loading: boolean;
  ar: boolean;
  initialDraft?: WorkspaceItem | null;
  onSave: (item: WorkspaceItem) => Promise<void>;
  onDelete: (id: string) => Promise<void>;
  onSignIn: () => void;
  onUse: (item: WorkspaceItem) => void;
  onRefresh: () => void;
}) {
  const [draft, setDraft] = useState<WorkspaceItem | null>(
    initialDraft || null,
  );
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("all");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [confirmDelete, setConfirmDelete] = useState("");
  const t = (en: string, arabic: string) => (ar ? arabic : en);
  const label =
    kind === "project"
      ? t("Projects", "المشاريع")
      : kind === "task"
        ? t("Tasks", "المهام")
        : t("Knowledge", "المعرفة");
  const filtered = items.filter(
    (item) =>
      item.kind === kind &&
      `${item.title} ${item.body}`
        .toLowerCase()
        .includes(search.toLowerCase()) &&
      (kind !== "task" ||
        filter === "all" ||
        item.completed === (filter === "done")),
  );
  async function save(e: FormEvent) {
    e.preventDefault();
    if (!draft) return;
    if (!signedIn) {
      onSignIn();
      return;
    }
    setBusy(true);
    setError("");
    try {
      await onSave(draft);
      setDraft(null);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }
  async function remove(id: string) {
    setBusy(true);
    setError("");
    try {
      await onDelete(id);
      setConfirmDelete("");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }
  async function toggle(item: WorkspaceItem) {
    setBusy(true);
    setError("");
    try {
      await onSave({ ...item, completed: !item.completed });
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="workspace enter">
      <div className="eyebrow">{t("YOUR WORKSPACE", "مساحة عملك")}</div>
      <div className="page-heading">
        <div>
          <h1>{label}</h1>
          <p>
            {kind === "project"
              ? t(
                  "Keep a goal and its instructions together. Use them as context in a conversation.",
                  "اجمع أهدافك وتعليماتك واستخدمها كسياق للمحادثة.",
                )
              : kind === "task"
                ? t(
                    "Keep track of next steps and due dates. Tasks are a checklist, not scheduled automations.",
                    "تابع الخطوات التالية ومواعيدها. المهام قائمة متابعة وليست عمليات تلقائية.",
                  )
                : t(
                    "Save useful text, reference material, and ideas for your next conversation.",
                    "احفظ النصوص المفيدة والمراجع والأفكار لمحادثتك القادمة.",
                  )}
          </p>
        </div>
        <button
          className="primary"
          onClick={() => {
            setDraft(newItem(kind));
            setError("");
          }}
        >
          <Icon name="plus" />
          {t("Create new", "إنشاء جديد")}
        </button>
      </div>
      {!signedIn && (
        <div className="save-banner">
          <Icon name="shield" />
          <span>
            {t(
              "Sign in to save your work privately and access it on another device.",
              "سجّل الدخول لحفظ عملك بشكل خاص والوصول إليه من جهاز آخر.",
            )}
          </span>
          <button className="text-button" onClick={onSignIn}>
            {t("Sign in", "تسجيل الدخول")}
            <Icon name="arrow" size={15} />
          </button>
        </div>
      )}
      {error && (
        <div className="error" role="alert">
          {error}
        </div>
      )}
      {draft && (
        <form className="record-editor" onSubmit={save}>
          <div className="panel-heading">
            <h2>{t("Edit details", "تعديل التفاصيل")}</h2>
            <button
              type="button"
              className="icon-button"
              aria-label={t("Close editor", "إغلاق المحرر")}
              onClick={() => setDraft(null)}
            >
              <Icon name="close" />
            </button>
          </div>
          <label>
            {t("Title", "العنوان")}
            <input
              autoFocus
              required
              maxLength={120}
              value={draft.title}
              onChange={(e) => setDraft({ ...draft, title: e.target.value })}
              placeholder={
                kind === "project"
                  ? t("What are you working on?", "ما المشروع الذي تعمل عليه؟")
                  : t("Give it a name", "أضف عنواناً")
              }
            />
          </label>
          <label>
            {kind === "project"
              ? t("Goals & instructions", "الأهداف والتعليمات")
              : t("Details", "التفاصيل")}
            <textarea
              value={draft.body}
              maxLength={12000}
              onChange={(e) => setDraft({ ...draft, body: e.target.value })}
              rows={7}
              dir="auto"
            />
          </label>
          {kind === "task" && (
            <label>
              {t("Due date (optional)", "الموعد النهائي (اختياري)")}
              <input
                type="date"
                value={draft.dueDate}
                onChange={(e) =>
                  setDraft({ ...draft, dueDate: e.target.value })
                }
              />
            </label>
          )}
          <div className="panel-actions">
            <button className="primary" disabled={busy || !draft.title.trim()}>
              {busy
                ? t("Saving…", "جارٍ الحفظ…")
                : signedIn
                  ? t("Save", "حفظ")
                  : t("Sign in to save", "سجّل الدخول للحفظ")}
              <Icon name="check" />
            </button>
            <span className="muted">
              {draft.body.length.toLocaleString()} / 12,000
            </span>
          </div>
        </form>
      )}
      <div className="record-toolbar">
        <label className="search-field">
          <Icon name="search" />
          <input
            aria-label={t("Search saved work", "البحث في العمل المحفوظ")}
            placeholder={t("Search saved work…", "ابحث في العمل المحفوظ…")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </label>
        {kind === "task" && (
          <select
            aria-label={t("Task filter", "تصفية المهام")}
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="all">{t("All tasks", "كل المهام")}</option>
            <option value="open">{t("To do", "قيد التنفيذ")}</option>
            <option value="done">{t("Completed", "مكتملة")}</option>
          </select>
        )}
        <button
          className="icon-button"
          aria-label={t("Refresh saved work", "تحديث العمل المحفوظ")}
          onClick={onRefresh}
          disabled={loading || !signedIn}
        >
          <Icon name="refresh" />
        </button>
      </div>
      {loading ? (
        <div className="skeleton-list">
          <span />
          <span />
          <span />
        </div>
      ) : !filtered.length ? (
        <div className="empty-state">
          <span className="empty-symbol">
            <Icon
              name={
                kind === "project"
                  ? "folder"
                  : kind === "task"
                    ? "check"
                    : "book"
              }
              size={34}
            />
          </span>
          <h3>
            {search
              ? t("No matching items", "لا توجد نتائج مطابقة")
              : t("A clear space for your next idea.", "مساحة لفكرتك القادمة.")}
          </h3>
          <p>
            {t(
              "Create your first item using the button above.",
              "أنشئ أول عنصر باستخدام الزر أعلاه.",
            )}
          </p>
        </div>
      ) : (
        <div className="record-list">
          {filtered.map((item) => (
            <article
              className={`record ${item.completed ? "completed" : ""}`}
              key={item.id}
            >
              {kind === "task" ? (
                <button
                  className={`task-check ${item.completed ? "checked" : ""}`}
                  disabled={busy}
                  aria-label={
                    item.completed
                      ? t("Mark incomplete", "تحديد كغير مكتملة")
                      : t("Mark complete", "تحديد كمكتملة")
                  }
                  onClick={() => toggle(item)}
                >
                  {item.completed && <Icon name="check" size={15} />}
                </button>
              ) : (
                <span className="record-icon">
                  <Icon name={kind === "project" ? "folder" : "file"} />
                </span>
              )}
              <div className="record-content">
                <button
                  className="record-title"
                  onClick={() => {
                    setDraft(item);
                    setError("");
                  }}
                >
                  {item.title}
                </button>
                <p>
                  {item.body || t("No details yet.", "لا توجد تفاصيل بعد.")}
                </p>
                <small>
                  {item.dueDate
                    ? `${t("Due", "الموعد")} ${item.dueDate}`
                    : new Date(item.updatedAt).toLocaleDateString(
                        ar ? "ar" : "en",
                        { month: "short", day: "numeric" },
                      )}
                </small>
              </div>
              <div className="record-actions">
                {kind !== "task" && (
                  <button className="text-button" onClick={() => onUse(item)}>
                    {t("Use in chat", "استخدام في المحادثة")}
                    <Icon name="arrow" size={15} />
                  </button>
                )}
                {confirmDelete === item.id ? (
                  <div className="delete-confirm">
                    <span>{t("Delete permanently?", "حذف نهائي؟")}</span>
                    <button
                      className="danger-button"
                      disabled={busy}
                      onClick={() => remove(item.id)}
                    >
                      {t("Delete", "حذف")}
                    </button>
                    <button
                      className="text-button"
                      onClick={() => setConfirmDelete("")}
                    >
                      {t("Cancel", "إلغاء")}
                    </button>
                  </div>
                ) : (
                  <button
                    className="icon-button"
                    aria-label={`${t("Delete", "حذف")} ${item.title}`}
                    onClick={() => setConfirmDelete(item.id)}
                  >
                    <Icon name="trash" size={17} />
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
      {items.length >= 100 && (
        <p className="footnote">
          {t(
            "Search covers loaded items. Load older saved work to search further back.",
            "يشمل البحث العناصر المحمّلة. حمّل الأعمال الأقدم لتوسيع البحث.",
          )}
        </p>
      )}
    </div>
  );
}
