import type { MetadataRoute } from "next";

// Makes the web app installable on Android, iOS and desktop, and lets it be
// packaged as an Android APK (Trusted Web Activity) with tools like PWABuilder.
export default function manifest(): MetadataRoute.Manifest {
  return {
    id: "/",
    name: "OneShot AI",
    short_name: "oneShot",
    description:
      "Your personal workspace for AI conversations, practical tools, projects, and ideas.",
    start_url: "/",
    scope: "/",
    display: "standalone",
    display_override: ["window-controls-overlay", "standalone"],
    orientation: "any",
    background_color: "#0A0B0E",
    theme_color: "#0A0B0E",
    categories: ["productivity", "utilities"],
    icons: [
      { src: "/icons/icon-192.png", sizes: "192x192", type: "image/png", purpose: "any" },
      { src: "/icons/icon-512.png", sizes: "512x512", type: "image/png", purpose: "any" },
      { src: "/icons/icon-maskable-192.png", sizes: "192x192", type: "image/png", purpose: "maskable" },
      { src: "/icons/icon-maskable-512.png", sizes: "512x512", type: "image/png", purpose: "maskable" },
      { src: "/oneshot.svg", sizes: "any", type: "image/svg+xml", purpose: "any" },
    ],
  };
}
