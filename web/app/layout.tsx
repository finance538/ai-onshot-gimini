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
  icons: { icon: "/oneshot.svg" },
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
  themeColor: "#161816",
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
