import type { Metadata } from "next";
import { JetBrains_Mono } from "next/font/google";
import "@fontsource/space-grotesk/300.css";
import "@fontsource/space-grotesk/400.css";
import "@fontsource/space-grotesk/500.css";
import "@fontsource/space-grotesk/600.css";
import "@fontsource/space-grotesk/700.css";
import "./globals.css";

const jetbrainsMono = JetBrains_Mono({
  variable: "--font-jetbrains-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Intend — The Intent-Driven API Workspace",
  description:
    "Say what you want — not how to get it. No login. No Electron bloat. Fully native JavaFX API workspace.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        {/* Umami Analytics — privacy-friendly, no cookies */}
        {/* To enable: sign up at https://cloud.umami.is (free tier) or self-host, */}
        {/* then replace the data-website-id and src below and uncomment. */}
        {/*
        <script
          defer
          src="https://cloud.umami.is/script.js"
          data-website-id="YOUR_WEBSITE_ID"
        />
        */}
      </head>
      <body
        className={`${jetbrainsMono.variable} font-sans antialiased bg-background text-foreground`}
      >
        {children}
      </body>
    </html>
  );
}
