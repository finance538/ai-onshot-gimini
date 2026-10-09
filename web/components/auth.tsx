"use client";
import { useEffect, useRef, useState, type FormEvent } from "react";
import {
  acceptInvite,
  getSettings,
  login,
  requestPasswordRecovery,
  signup,
  updateUser,
  type Settings,
} from "@netlify/identity";
import { Icon } from "./icon";
import { BrandMark } from "./brand-mark";
export type AuthMode = "login" | "signup" | "forgot" | "reset" | "invite";
export function AuthDialog({
  onClose,
  initialMode = "login",
  inviteToken,
  ar,
}: {
  onClose: () => void;
  initialMode?: AuthMode;
  inviteToken?: string;
  ar: boolean;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  const [mode, setMode] = useState(initialMode);
  const [settings, setSettings] = useState<Settings | null>(null);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const t = (en: string, arabic: string) => (ar ? arabic : en);
  useEffect(() => {
    dialog.current?.showModal();
    getSettings()
      .then(setSettings)
      .catch(() =>
        setError(
          t(
            "Account services are unavailable. Try again after the site is deployed.",
            "خدمة الحسابات غير متاحة. حاول بعد نشر الموقع.",
          ),
        ),
      );
  }, []);
  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");
    try {
      if (mode === "login") {
        await login(email, password);
        onClose();
      }
      if (mode === "signup") {
        const user = await signup(email, password, { full_name: name });
        if (user.confirmedAt) onClose();
        else
          setMessage(
            t(
              "Check your email to confirm your account, then sign in.",
              "تحقق من بريدك لتأكيد الحساب ثم سجّل الدخول.",
            ),
          );
      }
      if (mode === "forgot") {
        await requestPasswordRecovery(email);
        setMessage(
          t(
            "If the address has an account, a password reset email is on its way.",
            "إذا كان للبريد حساب فستصلك رسالة لإعادة تعيين كلمة المرور.",
          ),
        );
      }
      if (mode === "reset") {
        await updateUser({ password });
        onClose();
      }
      if (mode === "invite" && inviteToken) {
        await acceptInvite(inviteToken, password);
        onClose();
      }
    } catch {
      setError(
        t(
          "Could not complete this request. Check your details, email confirmation, and connection, then try again.",
          "تعذّر إكمال الطلب. تحقق من بياناتك وتأكيد البريد والاتصال ثم حاول مجدداً.",
        ),
      );
    } finally {
      setBusy(false);
    }
  }
  const title = {
    login: t("Welcome back.", "أهلاً بعودتك."),
    signup: t("Make room for your ideas.", "مساحة لأفكارك."),
    forgot: t("Reset your password.", "إعادة تعيين كلمة المرور."),
    reset: t("Choose a new password.", "اختر كلمة مرور جديدة."),
    invite: t("Your workspace is waiting.", "مساحتك بانتظارك."),
  }[mode];
  return (
    <dialog ref={dialog} className="auth-dialog" onCancel={onClose}>
      <div className="dialog-top">
        <BrandMark size={38} title="OneShot" />
        <button
          className="icon-button"
          aria-label={t("Close", "إغلاق")}
          onClick={onClose}
        >
          <Icon name="close" />
        </button>
      </div>
      <div className="eyebrow">ONESHOT ACCOUNT</div>
      <h2>{title}</h2>
      <p className="muted">
        {t(
          "Keep conversations, projects, tasks, and notes together across devices.",
          "احفظ المحادثات والمشاريع والمهام والملاحظات عبر أجهزتك.",
        )}
      </p>
      <form onSubmit={submit} className="auth-form">
        {mode === "signup" && (
          <label>
            {t("Name", "الاسم")}
            <input
              autoComplete="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={100}
              required
            />
          </label>
        )}
        {!["reset", "invite"].includes(mode) && (
          <label>
            {t("Email", "البريد الإلكتروني")}
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
        )}
        {mode !== "forgot" && (
          <label>
            {t("Password", "كلمة المرور")}
            <input
              type="password"
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
              minLength={mode === "login" ? 1 : 10}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
        )}
        {error && (
          <div className="error" role="alert">
            {error}
          </div>
        )}
        {message && (
          <div className="notice" role="status">
            {message}
          </div>
        )}
        <button
          className="primary"
          disabled={
            busy || (mode === "signup" && (!settings || settings.disableSignup))
          }
        >
          {busy
            ? t("Please wait…", "يرجى الانتظار…")
            : mode === "login"
              ? t("Sign in", "تسجيل الدخول")
              : mode === "signup"
                ? t("Create account", "إنشاء حساب")
                : mode === "forgot"
                  ? t("Send reset email", "إرسال رابط إعادة التعيين")
                  : t("Save password", "حفظ كلمة المرور")}
          <Icon name="arrow" />
        </button>
      </form>
      <div className="auth-links">
        {mode === "login" ? (
          <>
            <button
              className="text-button"
              onClick={() => {
                setMode("forgot");
                setError("");
              }}
            >
              {t("Forgot password?", "نسيت كلمة المرور؟")}
            </button>
            {settings && !settings.disableSignup && (
              <button
                className="text-button"
                onClick={() => {
                  setMode("signup");
                  setError("");
                }}
              >
                {t("Create an account", "إنشاء حساب")}
              </button>
            )}
          </>
        ) : (
          <button
            className="text-button"
            onClick={() => {
              setMode("login");
              setError("");
              setMessage("");
            }}
          >
            {t("Back to sign in", "العودة لتسجيل الدخول")}
          </button>
        )}
      </div>
    </dialog>
  );
}
