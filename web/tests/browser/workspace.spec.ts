import { test, expect, type Page } from "@playwright/test";
import { utilities } from "../../lib/utilities";

test("offline chat keeps the draft and a stopped reply cannot overwrite a new conversation", async ({
  page,
  context,
}) => {
  let release: () => void = () => {};
  const pending = new Promise<void>((resolve) => {
    release = resolve;
  });
  let calls = 0;
  await page.route("**/api/chat", async (route) => {
    calls++;
    if (calls === 1) {
      await pending;
      await route
        .fulfill({ json: { text: "Old cancelled response" } })
        .catch(() => {});
    } else await route.fulfill({ json: { text: "Fresh response" } });
  });
  await page.goto("/");
  await page.getByLabel("Message OneShot AI").fill("Keep this draft");
  await context.setOffline(true);
  await expect(page.locator(".offline-banner")).toBeVisible();
  await page.getByLabel("Message OneShot AI").press("Enter");
  await expect(page.getByLabel("Message OneShot AI")).toHaveValue(
    "Keep this draft",
  );
  await context.setOffline(false);
  await page.getByRole("button", { name: "Send message", exact: true }).click();
  await expect.poll(() => calls).toBe(1);
  await page.getByRole("button", { name: "Stop generation" }).click();
  await page
    .getByRole("button", { name: "New conversation", exact: false })
    .click();
  await page.getByLabel("Message OneShot AI").fill("A new question");
  await page.getByRole("button", { name: "Send message", exact: true }).click();
  await expect(page.locator(".message.assistant")).toContainText(
    "Fresh response",
  );
  release();
  await expect(page.locator(".messages")).not.toContainText(
    "Old cancelled response",
  );
  await page.getByLabel("AI model").selectOption("gpt-4.1-mini");
  await expect(
    page.locator(".message.assistant .message-author"),
  ).toContainText("Gemini 2.5 Flash");
});

async function openTools(page: Page) {
  await page
    .getByRole("button", { name: "Tool library", exact: false })
    .click();
  await expect(
    page.getByRole("heading", { name: "Small tools. Big possibilities." }),
  ).toBeVisible();
}

test("desktop workspace loads without browser errors and is readable at mobile widths", async ({
  page,
}) => {
  const errors: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Your ideas, into motion." }),
  ).toBeVisible();
  await expect(page.getByLabel("AI model")).toHaveValue("gemini-2.5-flash");
  await page.screenshot({ path: "/tmp/oneshot-desktop.png", fullPage: true });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.screenshot({
    path: "/tmp/oneshot-mobile.png",
    fullPage: true,
    animations: "disabled",
  });
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  expect(errors).toEqual([]);
});

test("all text tools execute their examples through the interface", async ({
  page,
}) => {
  test.setTimeout(90000);
  await page.goto("/");
  await openTools(page);
  for (const tool of utilities.filter(
    (tool) => !["image", "qr"].includes(tool.id),
  )) {
    await page
      .getByRole("button", {
        name: new RegExp(
          `ON DEVICE.*${tool.name.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}`,
        ),
      })
      .click();
    await page.getByRole("button", { name: "Try an example" }).click();
    await page.getByRole("button", { name: "Run tool", exact: true }).click();
    if (tool.id === "markdown")
      await expect(page.locator(".preview-output")).toContainText(
        "A fresh start",
      );
    else await expect(page.getByLabel("Tool result")).not.toHaveValue("");
    await expect(page.locator(".error[role=alert]")).toHaveCount(0);
    await page.getByRole("button", { name: "All tools", exact: true }).click();
  }
});

test("QR code and image tools produce real downloadable files", async ({
  page,
}) => {
  await page.goto("/");
  await openTools(page);
  await page.getByRole("button", { name: /ON DEVICE.*QR code maker/ }).click();
  await page.getByRole("button", { name: "Try an example" }).click();
  await page.getByRole("button", { name: "Run tool", exact: true }).click();
  await expect(page.getByAltText("Generated QR code")).toBeVisible();
  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Download", exact: true }).click();
  expect((await download).suggestedFilename()).toBe("oneshot.png");
  await page.getByRole("button", { name: "All tools", exact: true }).click();
  await page.getByRole("button", { name: /ON DEVICE.*Image resizer/ }).click();
  const image = await page.evaluate(() => {
    const canvas = document.createElement("canvas");
    canvas.width = 600;
    canvas.height = 300;
    const ctx = canvas.getContext("2d")!;
    ctx.fillStyle = "#e59470";
    ctx.fillRect(0, 0, 600, 300);
    return canvas.toDataURL().split(",")[1];
  });
  await page.locator("input[type=file]").setInputFiles({
    name: "sample.png",
    mimeType: "image/png",
    buffer: Buffer.from(image, "base64"),
  });
  await page.getByLabel("Maximum width (px)").fill("300");
  await page.getByRole("button", { name: "Run tool", exact: true }).click();
  await expect(page.getByAltText("Resized image")).toBeVisible();
  await expect(page.locator(".image-output")).toContainText("300 × 150 px");
  const resizedDownload = page.waitForEvent("download");
  await page.getByRole("button", { name: "Download", exact: true }).click();
  expect((await resizedDownload).suggestedFilename()).toBe("oneshot.webp");
});

test("search, validation, and safe Markdown rendering work", async ({
  page,
}) => {
  await page.goto("/");
  await openTools(page);
  await page.getByLabel("Search tools").fill("calculator");
  await expect(page.locator(".tool-card")).toHaveCount(1);
  await page.locator(".tool-card").click();
  await page.getByLabel("Tool input").fill("1/0");
  await page.getByRole("button", { name: "Run tool", exact: true }).click();
  await expect(page.locator(".error[role=alert]")).toContainText(
    "no finite result",
  );
  await page.getByLabel("Tool input").fill("(5+3)*2");
  await page.getByRole("button", { name: "Run tool", exact: true }).click();
  await expect(page.getByLabel("Tool result")).toHaveValue("16");
  await page.getByRole("button", { name: "All tools", exact: true }).click();
  await page.getByLabel("Search tools").fill("Markdown");
  await page.locator(".tool-card").click();
  await page
    .getByLabel("Tool input")
    .fill(
      '# Safe\n<script>document.body.innerHTML="bad"</script>\n[link](javascript:alert(1))',
    );
  await page.getByRole("button", { name: "Run tool", exact: true }).click();
  await expect(page.locator(".preview-output h1")).toHaveText("Safe");
  await expect(page.locator(".preview-output script")).toHaveCount(0);
  await expect(page.locator(".preview-output a")).not.toHaveAttribute(
    "href",
    /javascript/,
  );
});

test("chat handles failure, retries without duplicate user messages, and exports answers", async ({
  page,
}) => {
  let requests = 0;
  await page.route("**/api/chat", async (route) => {
    requests++;
    const body = route.request().postDataJSON();
    expect(
      body.messages.filter(
        (message: { role: string }) => message.role === "user",
      ),
    ).toHaveLength(1);
    if (requests === 1)
      await route.fulfill({
        status: 502,
        json: { error: "Temporary provider failure." },
      });
    else
      await route.fulfill({
        json: {
          text: "## A useful answer\n\nKeep the steps clear.",
          model: body.model,
        },
      });
  });
  await page.goto("/");
  await page.getByLabel("Message OneShot AI").fill("Help me organize an idea.");
  await page.getByRole("button", { name: "Send message", exact: true }).click();
  await expect(page.locator(".error[role=alert]")).toContainText(
    "Temporary provider failure",
  );
  await page.getByRole("button", { name: "Retry", exact: true }).click();
  await expect(page.locator(".message.assistant")).toContainText(
    "A useful answer",
  );
  await expect(page.locator(".message.user")).toHaveCount(1);
  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Export conversation" }).click();
  expect((await download).suggestedFilename()).toBe("oneshot-conversation.md");
  await page.getByRole("button", { name: "Save note", exact: true }).click();
  await expect(page.getByLabel("Details")).toContainText("A useful answer");
});

test("mobile navigation, Arabic direction, theme, and guest saving stay usable", async ({
  page,
}) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  await page.getByLabel("Message OneShot AI").fill("line one");
  await page.getByLabel("Message OneShot AI").press("Enter");
  await expect(page.getByLabel("Message OneShot AI")).toHaveValue("line one\n");
  await page.getByRole("button", { name: "Open navigation" }).click();
  await page.getByRole("button", { name: "Projects", exact: true }).click();
  await page.getByRole("button", { name: "Create new" }).click();
  await page.getByLabel("Title", { exact: true }).fill("My project");
  await page
    .getByLabel("Goals & instructions")
    .fill("Keep the message focused.");
  await page
    .getByRole("button", { name: "Sign in to save", exact: true })
    .click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await page.getByRole("button", { name: "Close", exact: true }).click();
  await expect(page.getByLabel("Title", { exact: true })).toHaveValue(
    "My project",
  );
  await page.getByRole("button", { name: "التبديل إلى العربية" }).click();
  await expect(page.locator("html")).toHaveAttribute("dir", "rtl");
  await page.getByRole("button", { name: "تبديل المظهر" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "light");
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.screenshot({ path: "/tmp/oneshot-arabic.png", fullPage: true });
});

test("API rejects unauthenticated storage, malformed requests, and unavailable Hermes", async ({
  request,
}) => {
  expect((await request.get("/api/workspace")).status()).toBe(401);
  expect(
    (
      await request.put("/api/workspace", { data: { title: "No access" } })
    ).status(),
  ).toBe(401);
  expect(
    (
      await request.delete(
        "/api/workspace?id=00000000-0000-4000-8000-000000000000",
      )
    ).status(),
  ).toBe(401);
  expect((await request.get("/api/chat")).status()).toBe(405);
  expect(
    (
      await request.post("/api/chat", {
        data: { messages: [{ role: "system", content: "no" }] },
      })
    ).status(),
  ).toBe(400);
  expect(
    (
      await request.post("/api/chat", {
        data: { messages: [{ role: "user", content: "hello" }], model: "fake" },
      })
    ).status(),
  ).toBe(400);
  expect(
    (await request.post("/api/hermes", { data: { message: 12 } })).status(),
  ).toBe(400);
  const status = await (await request.get("/api/status")).json();
  if (!status.hermes)
    expect(
      (
        await request.post("/api/hermes", { data: { message: "hello" } })
      ).status(),
    ).toBe(503);
});

test("signed-in UI creates, edits, reloads, and deletes private work with mocked services", async ({
  page,
}) => {
  // These fixtures exercise browser behavior. Production database migrations are
  // applied by Netlify at deploy time, never by the test runner.
  const account = {
    id: crypto.randomUUID(),
    email: "workspace@example.test",
    confirmed_at: new Date().toISOString(),
    user_metadata: { full_name: "Workspace test" },
    app_metadata: { provider: "email" },
  };
  const encode = (value: unknown) =>
    Buffer.from(JSON.stringify(value)).toString("base64url");
  const access = `${encode({ alg: "none" })}.${encode({ sub: account.id, email: account.email, exp: Math.floor(Date.now() / 1000) + 3600 })}.browser-test-only`;
  const records = new Map<string, Record<string, unknown>>();
  await page.route("**/.netlify/identity/**", async (route) => {
    const path = new URL(route.request().url()).pathname;
    if (path.endsWith("/settings"))
      await route.fulfill({
        json: {
          autoconfirm: true,
          disable_signup: false,
          external: { email: true },
        },
      });
    else if (path.endsWith("/token"))
      await route.fulfill({
        json: {
          access_token: access,
          refresh_token: "browser-test-only",
          token_type: "bearer",
          expires_in: 3600,
        },
      });
    else if (path.endsWith("/user")) await route.fulfill({ json: account });
    else if (path.endsWith("/logout")) await route.fulfill({ status: 204 });
    else await route.fulfill({ status: 404 });
  });
  await page.route("**/api/workspace*", async (route) => {
    const method = route.request().method();
    const id = new URL(route.request().url()).searchParams.get("id");
    if (method === "PUT") {
      const item = {
        ...route.request().postDataJSON(),
        updatedAt: new Date().toISOString(),
      };
      records.set(item.id, item);
      await route.fulfill({ json: { item } });
    } else if (method === "DELETE") {
      records.delete(id!);
      await route.fulfill({ json: { deleted: true } });
    } else if (id) await route.fulfill({ json: { item: records.get(id) } });
    else
      await route.fulfill({
        json: { items: [...records.values()], nextOffset: null },
      });
  });
  await page.goto("/");
  await page
    .getByRole("button", { name: /Personal workspace.*Sign in to save/ })
    .click();
  await page.getByLabel("Email", { exact: true }).fill(account.email);
  await page
    .getByLabel("Password", { exact: true })
    .fill("browser-fixture-password");
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await page.getByRole("button", { name: "Projects", exact: true }).click();
  await page.getByRole("button", { name: "Create new" }).click();
  await page.getByLabel("Title", { exact: true }).fill("Launch checklist");
  await page
    .getByLabel("Goals & instructions")
    .fill("Use clear, concise language.");
  await page.getByRole("button", { name: "Save", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Launch checklist", exact: true }),
  ).toBeVisible();
  await page
    .getByRole("button", { name: "Launch checklist", exact: true })
    .click();
  await page.getByLabel("Title", { exact: true }).fill("Updated checklist");
  await page.getByRole("button", { name: "Save", exact: true }).click();
  await page.reload();
  await page.getByRole("button", { name: "Projects", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Updated checklist", exact: true }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Use in chat", exact: true }).click();
  await expect(page.locator(".context-strip")).toContainText(
    "Updated checklist",
  );
  await page.getByRole("button", { name: "Projects", exact: true }).click();
  await page
    .getByRole("button", { name: "Delete Updated checklist", exact: true })
    .click();
  await page.getByRole("button", { name: "Delete", exact: true }).click();
  await expect(
    page.getByRole("button", { name: "Updated checklist", exact: true }),
  ).toHaveCount(0);
  expect(records.size).toBe(0);
});
