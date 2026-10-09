"use client";
import { useEffect, useRef, useState } from "react";
import { assistants } from "../lib/catalog";
import { runUtility, utilities, type UtilityId } from "../lib/utilities";
import { copyText, download } from "../lib/client";
import { Icon } from "./icon";
import { Markdown } from "./markdown";

export function ToolLibrary({
  onAssistant,
  onSaveNote,
  ar,
  onlyAssistants = false,
  hermes = false,
}: {
  onAssistant: (id: string) => void;
  onSaveNote: (text: string, title: string) => void;
  ar: boolean;
  onlyAssistants?: boolean;
  hermes?: boolean;
}) {
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [selected, setSelected] = useState<UtilityId | null>(null);
  const t = (en: string, arabic: string) => (ar ? arabic : en);
  if (selected)
    return (
      <Utility
        key={selected}
        id={selected}
        onBack={() => setSelected(null)}
        onSaveNote={onSaveNote}
        ar={ar}
      />
    );
  const matches = (
    name: string,
    description: string,
    group: string,
    ai: boolean,
  ) =>
    (category === "All" || category === group || (category === "AI" && ai)) &&
    `${name} ${description}`.toLowerCase().includes(search.toLowerCase());
  const shownUtilities = onlyAssistants
    ? []
    : utilities.filter((u) =>
        matches(u.name, u.description, u.category, false),
      );
  const shownAssistants = assistants.filter((a) =>
    matches(`${a.name} ${a.ar}`, a.description, a.category, true),
  );
  return (
    <div className="workspace enter">
      <div className="eyebrow">
        {t("THE WORKBENCH", "مساحة الأدوات")} <span className="tiny-line" />
      </div>
      <div className="page-heading">
        <div>
          <h1>
            {onlyAssistants
              ? t("A specialist for every idea.", "مساعد متخصص لكل فكرة.")
              : t(
                  "Small tools. Big possibilities.",
                  "أدوات صغيرة. إمكانات كبيرة.",
                )}
          </h1>
          <p>
            {onlyAssistants
              ? t(
                  "Choose a focused assistant, then pick your model.",
                  "اختر مساعداً متخصصاً ثم اختر النموذج.",
                )
              : t(
                  "Practical utilities and focused AI, all in one place.",
                  "أدوات عملية ومساعدون أذكياء في مكان واحد.",
                )}
          </p>
        </div>
        <span className="count-tag">
          {onlyAssistants
            ? assistants.length
            : utilities.length + assistants.length}{" "}
          {t("tools", "أداة")}
        </span>
      </div>
      <div className="library-controls">
        <label className="search-field">
          <Icon name="search" />
          <input
            aria-label={t("Search tools", "البحث في الأدوات")}
            placeholder={t("Find your next tool…", "ابحث عن أداة…")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </label>
        <div className="filter-row">
          {(onlyAssistants
            ? ["All", "Writing", "Work", "Development", "Learning"]
            : [
                "All",
                "AI",
                "Text",
                "Developer",
                "Design",
                "Security",
                "Everyday",
              ]
          ).map((c) => (
            <button
              key={c}
              className={category === c ? "chip selected" : "chip"}
              onClick={() => setCategory(c)}
            >
              {c === "All" ? t("All tools", "كل الأدوات") : c}
            </button>
          ))}
        </div>
      </div>
      <div className="tool-grid">
        {shownUtilities.map((u, i) => (
          <button
            className="tool-card"
            key={u.id}
            onClick={() => setSelected(u.id)}
            style={{ animationDelay: `${Math.min(i, 8) * 25}ms` }}
          >
            <div className="tool-top">
              <span className="tool-icon">
                <Icon name={u.icon} size={22} />
              </span>
              <span className="tool-kind">{t("ON DEVICE", "على الجهاز")}</span>
            </div>
            <h3>{u.name}</h3>
            <p>{u.description}</p>
            <div className="tool-bottom">
              <span>{u.category}</span>
              <Icon name="arrow" size={17} />
            </div>
          </button>
        ))}
        {shownAssistants.map((a) => (
          <button
            className="tool-card ai-card"
            key={a.id}
            onClick={() => onAssistant(a.id)}
          >
            <div className="tool-top">
              <span className="tool-icon">
                <Icon name={a.icon} size={22} />
              </span>
              <span className="tool-kind">AI ASSISTANT</span>
            </div>
            <h3>{ar ? a.ar : a.name}</h3>
            <p>{a.description}</p>
            <div className="tool-bottom">
              <span>{a.category}</span>
              <Icon name="arrow" size={17} />
            </div>
          </button>
        ))}
      </div>
      {!shownUtilities.length && !shownAssistants.length && (
        <div className="empty-state">
          <Icon name="search" size={32} />
          <h3>{t("No matching tools", "لا توجد أدوات مطابقة")}</h3>
          <p>
            {t("Try another keyword or category.", "جرّب كلمة أو فئة أخرى.")}
          </p>
        </div>
      )}
      {onlyAssistants && (
        <div className="connection-card">
          <Icon name="bot" size={30} />
          <div>
            <h3>OneShot–Hermes</h3>
            <p>
              {hermes
                ? t(
                    "Your external A2A agent is configured. Results depend on its connected tools.",
                    "تم إعداد وكيل A2A الخارجي. تعتمد النتائج على أدواته المتصلة.",
                  )
                : t(
                    "Connect a Hermes A2A service to use this agent. No connection is configured yet.",
                    "يتطلب هذا الوكيل اتصالاً بخدمة Hermes A2A. لم يتم إعداد الاتصال بعد.",
                  )}
            </p>
          </div>
          <button
            className="secondary"
            disabled={!hermes}
            onClick={() => onAssistant("hermes")}
          >
            {hermes
              ? t("Open agent", "فتح الوكيل")
              : t("Not connected", "غير متصل")}
          </button>
        </div>
      )}
      <p className="footnote">
        {t(
          "On-device tools run in your browser. AI assistants send your prompt to the selected provider; they do not access external apps or browse the web.",
          "تعمل أدوات الجهاز في متصفحك. ترسل أدوات الذكاء الاصطناعي طلبك إلى المزود المختار ولا تصل إلى تطبيقات خارجية أو تتصفح الويب.",
        )}
      </p>
    </div>
  );
}

function Utility({
  id,
  onBack,
  onSaveNote,
  ar,
}: {
  id: UtilityId;
  onBack: () => void;
  onSaveNote: (text: string, title: string) => void;
  ar: boolean;
}) {
  const tool = utilities.find((u) => u.id === id)!;
  const [input, setInput] = useState("");
  const [output, setOutput] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [imageBlob, setImageBlob] = useState<Blob | null>(null);
  const [file, setFile] = useState<File | null>(null);
  const [width, setWidth] = useState(1200);
  const [format, setFormat] = useState("image/webp");
  const [quality, setQuality] = useState(85);
  const fileInput = useRef<HTMLInputElement>(null);
  const t = (en: string, arabic: string) => (ar ? arabic : en);
  useEffect(
    () => () => {
      if (imageUrl.startsWith("blob:")) URL.revokeObjectURL(imageUrl);
    },
    [imageUrl],
  );
  async function run() {
    setBusy(true);
    setError("");
    setNotice("");
    setOutput("");
    setImageUrl("");
    setImageBlob(null);
    try {
      if (id === "qr") {
        if (!input.trim() || new TextEncoder().encode(input).length > 2000)
          throw new Error("Enter text or a link up to 2,000 bytes.");
        const QRCode = await import("qrcode");
        const url = await QRCode.toDataURL(input, {
          width: 768,
          margin: 3,
          errorCorrectionLevel: "M",
          color: { dark: "#20241f", light: "#ffffff" },
        });
        setImageUrl(url);
        setImageBlob(await (await fetch(url)).blob());
        setOutput(input);
      } else if (id === "image") {
        if (!file) throw new Error("Choose a JPG, PNG, or WebP image.");
        if (!Number.isInteger(width) || width < 16 || width > 4096)
          throw new Error("Choose a width from 16 to 4,096 pixels.");
        const url = URL.createObjectURL(file);
        try {
          const image = new window.Image();
          image.src = url;
          await image.decode();
          if (image.naturalWidth * image.naturalHeight > 25_000_000)
            throw new Error("Choose an image smaller than 25 megapixels.");
          const targetWidth = Math.min(width, image.naturalWidth);
          const targetHeight = Math.round(
            (image.naturalHeight * targetWidth) / image.naturalWidth,
          );
          if (targetHeight > 8192)
            throw new Error("This image is too tall. Choose a smaller width.");
          const canvas = document.createElement("canvas");
          canvas.width = targetWidth;
          canvas.height = targetHeight;
          const ctx = canvas.getContext("2d");
          if (!ctx)
            throw new Error("Image processing is unavailable in this browser.");
          if (format === "image/jpeg") {
            ctx.fillStyle = "#ffffff";
            ctx.fillRect(0, 0, targetWidth, targetHeight);
          }
          ctx.drawImage(image, 0, 0, targetWidth, targetHeight);
          const blob = await new Promise<Blob>((resolve, reject) =>
            canvas.toBlob(
              (b) =>
                b
                  ? resolve(b)
                  : reject(new Error("Could not export the image.")),
              format,
              quality / 100,
            ),
          );
          setImageBlob(blob);
          setImageUrl(URL.createObjectURL(blob));
          setOutput(
            `${targetWidth} × ${targetHeight} px · ${(blob.size / 1024).toFixed(1)} KB\nOriginal: ${(file.size / 1024).toFixed(1)} KB`,
          );
        } finally {
          URL.revokeObjectURL(url);
        }
      } else setOutput(await runUtility(id, input));
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Something went wrong. Check your input.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function copy() {
    try {
      await copyText(output);
      setNotice(t("Copied to clipboard", "تم النسخ"));
    } catch (err) {
      setError((err as Error).message);
    }
  }
  return (
    <div className="workspace utility-page enter">
      <button className="text-button back-button" onClick={onBack}>
        <Icon name="back" />
        {t("All tools", "كل الأدوات")}
      </button>
      <div className="eyebrow">{tool.category.toUpperCase()} / ON DEVICE</div>
      <div className="page-heading">
        <div>
          <h1>{tool.name}</h1>
          <p>{tool.description}</p>
        </div>
        <span className="large-tool-icon">
          <Icon name={tool.icon} size={32} />
        </span>
      </div>
      <div className="utility-grid">
        <section className="editor-panel">
          <div className="panel-heading">
            <h2>{t("Your input", "المدخلات")}</h2>
            {id !== "image" && (
              <button
                className="text-button"
                onClick={() => setInput(tool.example)}
              >
                {t("Try an example", "جرّب مثالاً")}
              </button>
            )}
          </div>
          {id === "image" ? (
            <div className="image-controls">
              <input
                ref={fileInput}
                type="file"
                accept="image/png,image/jpeg,image/webp"
                onChange={(e) => {
                  const picked = e.target.files?.[0];
                  if (!picked) return;
                  if (
                    picked.size > 10_000_000 ||
                    !["image/jpeg", "image/png", "image/webp"].includes(
                      picked.type,
                    )
                  ) {
                    setError("Choose a JPG, PNG, or WebP under 10 MB.");
                    return;
                  }
                  setFile(picked);
                  setError("");
                }}
              />
              <label>
                {t("Maximum width (px)", "أقصى عرض بالبكسل")}
                <input
                  type="number"
                  min={16}
                  max={4096}
                  value={width}
                  onChange={(e) => setWidth(Number(e.target.value))}
                />
              </label>
              <label>
                {t("Output format", "صيغة الصورة")}
                <select
                  value={format}
                  onChange={(e) => setFormat(e.target.value)}
                >
                  <option value="image/webp">WebP</option>
                  <option value="image/jpeg">JPEG</option>
                  <option value="image/png">PNG</option>
                </select>
              </label>
              <label>
                {t("Quality", "الجودة")} · {quality}%
                <input
                  type="range"
                  min={10}
                  max={100}
                  value={quality}
                  onChange={(e) => setQuality(Number(e.target.value))}
                  disabled={format === "image/png"}
                />
              </label>
              <p className="muted">
                {t(
                  "Files stay on this device. Animated images are exported as a still frame.",
                  "تبقى الملفات على هذا الجهاز. تصدّر الصور المتحركة كصورة ثابتة.",
                )}
              </p>
            </div>
          ) : (
            <textarea
              className="tool-input"
              aria-label={t("Tool input", "مدخلات الأداة")}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={tool.example}
              maxLength={100000}
              spellCheck={false}
              dir="auto"
            />
          )}
          <div className="panel-actions">
            <button
              className="primary"
              disabled={busy || (id === "image" ? !file : !input.trim())}
              onClick={run}
            >
              {busy
                ? t("Working…", "جارٍ التنفيذ…")
                : t("Run tool", "تشغيل الأداة")}
              <Icon name="arrow" size={17} />
            </button>
            {id !== "image" && (
              <span className="muted">
                {input.length.toLocaleString()} {t("characters", "حرف")}
              </span>
            )}
          </div>
        </section>
        <section className="editor-panel output-panel">
          <div className="panel-heading">
            <h2>{t("Result", "النتيجة")}</h2>
            <span className="badge">
              <span className="status-dot" />
              {t("Private · on device", "خاص · على الجهاز")}
            </span>
          </div>
          {imageUrl ? (
            <div className="image-output">
              <img
                src={imageUrl}
                alt={id === "qr" ? "Generated QR code" : "Resized image"}
              />
              <p>{output}</p>
            </div>
          ) : output ? (
            id === "markdown" ? (
              <div className="preview-output">
                <Markdown text={output} />
              </div>
            ) : (
              <textarea
                className="tool-output"
                aria-label={t("Tool result", "نتيجة الأداة")}
                value={output}
                readOnly
                dir="auto"
              />
            )
          ) : (
            <div className="result-placeholder">
              <Icon name={tool.icon} size={36} />
              <p>
                {t(
                  "A little input. A useful result.",
                  "مدخلات بسيطة. نتيجة مفيدة.",
                )}
              </p>
              <span>{t("Your result appears here.", "تظهر النتيجة هنا.")}</span>
            </div>
          )}
          {(output || imageBlob) && (
            <div className="panel-actions result-actions">
              {!imageBlob && (
                <button className="secondary" onClick={copy}>
                  <Icon name="copy" size={16} />
                  {t("Copy", "نسخ")}
                </button>
              )}
              <button
                className="secondary"
                onClick={() =>
                  download(
                    imageBlob
                      ? `oneshot.${imageBlob.type.split("/")[1] === "jpeg" ? "jpg" : imageBlob.type.split("/")[1]}`
                      : `${id}.${id.includes("json") && id !== "json-csv" ? "json" : id === "json-csv" ? "csv" : id === "markdown" ? "md" : "txt"}`,
                    imageBlob || output,
                  )
                }
              >
                <Icon name="download" size={16} />
                {t("Download", "تنزيل")}
              </button>
              {!imageBlob && (
                <button
                  className="text-button"
                  onClick={() => onSaveNote(output, tool.name)}
                >
                  {t("Save as note", "حفظ كملاحظة")}
                </button>
              )}
            </div>
          )}
        </section>
      </div>
      {error && (
        <div className="error" role="alert">
          {error}
        </div>
      )}
      {notice && (
        <div className="notice" role="status">
          {notice}
        </div>
      )}
      <p className="footnote">
        {id === "password"
          ? t(
              "Passwords use your browser’s cryptographic random generator. Store the result in a password manager.",
              "تستخدم كلمات المرور مولّد الأرقام العشوائية الآمن في متصفحك. احفظها في مدير كلمات المرور.",
            )
          : t(
              "No upload. No AI credits. This tool runs entirely in your browser.",
              "بدون رفع ملفات أو استهلاك رصيد ذكاء اصطناعي. تعمل الأداة بالكامل في متصفحك.",
            )}
      </p>
    </div>
  );
}
