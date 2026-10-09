import type { Metadata, Viewport } from "next";
import "@fontsource/dm-sans/400.css";
import "@fontsource/dm-sans/500.css";
import "@fontsource/dm-sans/600.css";
import "@fontsource/dm-sans/700.css";
import "@fontsource/instrument-serif/400-italic.css";
import "./globals.css";
export const metadata: Metadata = {
  title: "OneShot AI — Your ideas, into motion",
  description:
    "Your personal workspace for AI conversations, practical tools, projects, and ideas.",
  applicationName: "OneShot AI",
  icons: {
    icon: [
      { url: "/favicon.ico", sizes: "48x48" },
      { url: "/oneshot.svg", type: "image/svg+xml" },
      { url: "/icons/icon-192.png", sizes: "192x192", type: "image/png" },
    ],
    apple: [{ url: "/icons/apple-touch-icon.png", sizes: "180x180" }],
  },
  appleWebApp: {
    capable: true,
    title: "OneShot AI",
    statusBarStyle: "black-translucent",
  },
};
export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
  themeColor: [
    { media: "(prefers-color-scheme: dark)", color: "#0A0B0E" },
    { media: "(prefers-color-scheme: light)", color: "#f5f3ed" },
  ],
};
export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" data-theme="dark">
      <body>{children}</body>
    </html>
  );
}
