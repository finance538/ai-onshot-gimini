import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./tests/browser",
  timeout: 30000,
  fullyParallel: false,
  workers: 1,
  use: {
    baseURL: "http://localhost:8889",
    headless: true,
    viewport: { width: 1440, height: 1000 },
  },
  reporter: "list",
});
