"use client";

import { useState, useEffect } from "react";
import {
  Download,
  Github,
  CheckCircle2,
  XCircle,
  Menu,
  X,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import {
  Terminal,
  TypingAnimation,
  AnimatedSpan,
} from "@/components/ui/terminal";
import { GlowingEffect } from "@/components/ui/glowing-effect";
import PlugConnectedIcon from "@/components/ui/plug-connected-icon";
import Link from "next/link";

/* ─── GITHUB STARS ─── */

function GitHubStars() {
  const [count, setCount] = useState<number | null>(null);

  useEffect(() => {
    fetch("https://api.github.com/repos/pskuntal1248/http-client-intend")
      .then((r) => r.json())
      .then((d) => {
        if (d.stargazers_count !== undefined) setCount(d.stargazers_count);
      })
      .catch(() => { });
  }, []);

  return (
    <a
      href="https://github.com/pskuntal1248/http-client-intend"
      target="_blank"
      rel="noopener noreferrer"
      className="hidden md:inline-flex items-center gap-1.5 rounded-full border border-neutral-800 bg-neutral-900/60 px-3 py-1 text-xs text-neutral-400 transition hover:border-neutral-700 hover:text-neutral-300"
    >
      <svg viewBox="0 0 16 16" fill="currentColor" className="h-3.5 w-3.5">
        <path d="M8 .25a.75.75 0 01.673.418l1.882 3.815 4.21.612a.75.75 0 01.416 1.279l-3.046 2.97.719 4.192a.75.75 0 01-1.088.791L8 12.347l-3.766 1.98a.75.75 0 01-1.088-.79l.72-4.194L.818 6.374a.75.75 0 01.416-1.28l4.21-.611L7.327.668A.75.75 0 018 .25z" />
      </svg>
      {count !== null ? count : "–"}
    </a>
  );
}

/* ─── NAV ─── */

function Nav() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-50 border-b border-neutral-800/80 bg-black/70 backdrop-blur-xl">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-6">
        {/* Logo */}
        <div className="flex items-center gap-2.5">
          <img src="/logo.png" alt="Intend" className="h-7 w-auto" />
          <span className="text-base font-bold text-white">Intend</span>
        </div>

        {/* Desktop links */}
        <div className="hidden items-center gap-6 text-sm text-neutral-500 md:flex">
          <Link href="#features" className="transition hover:text-neutral-300">
            Features
          </Link>
          <Link href="/docs" className="transition hover:text-neutral-300">
            Docs
          </Link>
          <a
            href="https://github.com/pskuntal1248/http-client-intend"
            target="_blank"
            rel="noopener noreferrer"
            className="transition hover:text-neutral-300"
          >
            GitHub
          </a>
        </div>

        {/* Right side */}
        <div className="flex items-center gap-3">
          <GitHubStars />
          <Link
            href="/download"
            className="rounded-lg bg-white px-4 py-1.5 text-sm font-medium text-black transition hover:-translate-y-0.5 hover:bg-neutral-200"
          >
            Download
          </Link>
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden rounded-lg border border-neutral-800 p-1.5 text-neutral-400 transition hover:bg-neutral-900 hover:text-white"
            aria-label="Toggle menu"
          >
            {mobileOpen ? (
              <X className="h-5 w-5" />
            ) : (
              <Menu className="h-5 w-5" />
            )}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="overflow-hidden border-t border-neutral-800/60 md:hidden"
          >
            <div className="mx-auto flex max-w-6xl flex-col gap-1 px-6 py-3">
              <Link
                href="#features"
                onClick={() => setMobileOpen(false)}
                className="rounded-lg px-3 py-2.5 text-sm text-neutral-400 transition hover:bg-neutral-900 hover:text-white"
              >
                Features
              </Link>
              <Link
                href="/docs"
                onClick={() => setMobileOpen(false)}
                className="rounded-lg px-3 py-2.5 text-sm text-neutral-400 transition hover:bg-neutral-900 hover:text-white"
              >
                Docs
              </Link>
              <a
                href="https://github.com/pskuntal1248/http-client-intend"
                target="_blank"
                rel="noopener noreferrer"
                className="rounded-lg px-3 py-2.5 text-sm text-neutral-400 transition hover:bg-neutral-900 hover:text-white"
              >
                GitHub
              </a>
              <Link
                href="/download"
                onClick={() => setMobileOpen(false)}
                className="mt-1 rounded-lg bg-neutral-900 px-3 py-2.5 text-center text-sm font-medium text-white transition hover:bg-neutral-800"
              >
                Download
              </Link>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
}

/* ─── HERO ─── */

const headline = "The API client that reads your intent".split(" ");

function Hero() {
  return (
    <section className="relative mx-auto max-w-6xl">
      <div className="px-6 py-14 sm:py-20 md:py-24">
        <h1 className="mx-auto max-w-3xl text-center text-2xl font-bold leading-tight text-neutral-300 sm:text-3xl md:text-4xl lg:text-6xl">
          {headline.map((word, i) => (
            <motion.span
              key={i}
              initial={{ opacity: 0, filter: "blur(4px)", y: 10 }}
              animate={{ opacity: 1, filter: "blur(0px)", y: 0 }}
              transition={{ duration: 0.3, delay: i * 0.08, ease: "easeInOut" }}
              className="mr-2 inline-block sm:mr-3"
            >
              {word}
            </motion.span>
          ))}
        </h1>

        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3, delay: 0.7 }}
          className="mx-auto mt-5 max-w-lg text-center text-sm text-neutral-500 sm:text-base"
        >
          Select a method, pick auth from a dropdown, type your URL — Intend
          resolves every HTTP header automatically. Native JavaFX desktop app.
          No Electron. No account. Fully offline.
        </motion.p>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3, delay: 0.9 }}
          className="mt-8 flex flex-wrap items-center justify-center gap-4"
        >
          <Link
            href="/download"
            className="w-44 rounded-lg bg-red-600 px-6 py-2.5 text-center text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-red-500 shadow-[0_0_15px_rgba(220,38,38,0.3)] hover:shadow-[0_0_20px_rgba(220,38,38,0.5)]"
          >
            <Download className="mr-2 inline h-4 w-4" />
            Download
          </Link>
          <a
            href="https://github.com/pskuntal1248/http-client-intend"
            target="_blank"
            rel="noopener noreferrer"
            className="w-44 rounded-lg border border-neutral-700 px-6 py-2.5 text-center text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-neutral-900"
          >
            <Github className="mr-2 inline h-4 w-4" />
            Source
          </a>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 1.1 }}
          className="mx-auto mt-12 max-w-[74rem] sm:mt-16"
        >
          <img
            src="/hero-ui.png"
            alt="Intend workspace"
            className="w-full rounded-xl border border-neutral-800/70 shadow-2xl"
          />
        </motion.div>
      </div>
    </section>
  );
}

/* ─── FEATURES — Glowing grid ─── */

const features = [
  {
    title: "Zero-config headers",
    desc: "Auto-detects Content-Type (JSON, XML, text), resolves auth headers from dropdowns, and adds browser-like headers for better WAF compatibility.",
    cardClass: "md:col-span-3 xl:col-span-5 min-h-[12rem]",
  },
  {
    title: "Stripe-style idempotency",
    desc: "POST/PUT/PATCH requests get Idempotency-Key and X-Request-ID automatically, with safe retries using the same key.",
    cardClass: "md:col-span-3 xl:col-span-4 min-h-[13rem]",
  },
  {
    title: "Response chaining",
    desc: "Capture values from a response and reuse them as {{variables}} in the next request without custom scripts.",
    cardClass: "md:col-span-6 xl:col-span-3 min-h-[14rem]",
  },
  {
    title: "Template engine",
    desc: "Use built-ins like {{uuid}}, {{timestamp}}, and {{randomEmail}}. Fresh values are generated on each send.",
    cardClass: "md:col-span-2 xl:col-span-4 min-h-[11rem]",
  },
  {
    title: "Environments & auth",
    desc: "Switch DEV/PROD quickly. Credentials load per environment with support for API key, Basic auth, and Bearer token.",
    cardClass: "md:col-span-4 xl:col-span-5 min-h-[12rem]",
  },
  {
    title: "Plugin architecture (SPI)",
    desc: "Extend the header engine with custom HeaderProvider plugins without changing the core.",
    cardClass: "md:col-span-6 xl:col-span-3 min-h-[11rem]",
  },
];

function Features() {
  return (
    <section id="features" className="py-16 sm:py-24">
      <div className="mx-auto max-w-6xl px-6">
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4 }}
        >
          <p className="text-sm font-medium text-red-500/90 tracking-wide">
            Features
          </p>
          <h2 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
            Everything you need to test APIs.
            <br />
            <span className="text-neutral-500">Nothing you don&apos;t.</span>
          </h2>
        </motion.div>

        <motion.ul
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="mt-10 grid grid-cols-1 gap-3.5 md:grid-cols-6 xl:grid-cols-12"
        >
          {features.map((f) => (
            <li
              key={f.title}
              className={`list-none ${f.cardClass}`}
            >
              <div className="relative h-full rounded-2xl border border-neutral-800 p-2">
                <GlowingEffect
                  spread={40}
                  glow={true}
                  proximity={64}
                  inactiveZone={0.01}
                />
                <div className="relative flex h-full flex-col gap-3 overflow-hidden rounded-xl border border-neutral-800/50 bg-neutral-950 p-4 shadow-[0px_0px_27px_0px_#1a1a1a]">
                  <div className="flex flex-col gap-3">
                    <div className="w-fit rounded-lg border border-neutral-700 p-2">
                      <PlugConnectedIcon size={16} className="text-neutral-400" />
                    </div>
                    <div className="space-y-2">
                      <h3 className="text-lg font-semibold text-white">
                        {f.title}
                      </h3>
                      <p className="text-sm leading-relaxed text-neutral-400">
                        {f.desc}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          ))}
        </motion.ul>
      </div>
    </section>
  );
}

/* ─── HOW IT WORKS — Pipeline ─── */

const pipeline = [
  {
    label: "01",
    title: "Define intent",
    items: ["Method (GET, POST, PUT, DELETE, PATCH)", "URL with {{variables}}", "Auth strategy from dropdown", "JSON / XML / File payload"],
  },
  {
    label: "02",
    title: "Engine resolves",
    items: ["ProtocolProvider → Content-Type, User-Agent, Sec-*", "IdempotencyProvider → Idempotency-Key, X-Request-ID", "AuthProvider → Authorization / X-API-KEY"],
  },
  {
    label: "03",
    title: "Execute & capture",
    items: ["HTTP/2 with TLS", "Auto-retry on 502/503/504 (same key)", "Capture response values → {{variable}}", "Pretty-print JSON, show status/time/size"],
  },
];

function Pipeline() {
  return (
    <section className="border-y border-neutral-800/80 py-16 sm:py-24">
      <div className="mx-auto max-w-6xl px-6">
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4 }}
        >
          <p className="text-sm font-medium text-red-500/90 tracking-wide">
            How it works
          </p>
          <h2 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
            From intent to response in three steps.
          </h2>
        </motion.div>

        <div className="mt-10 grid grid-cols-1 gap-5 md:grid-cols-3">
          {pipeline.map((step, i) => (
            <motion.div
              key={step.label}
              initial={{ opacity: 0, y: 10 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.3, delay: i * 0.12 }}
              className="rounded-2xl border border-neutral-800 bg-neutral-950 p-5"
            >
              <span className="font-mono text-xs text-neutral-600">
                {step.label}
              </span>
              <h3 className="mt-3 text-lg font-semibold text-white">
                {step.title}
              </h3>
              <ul className="mt-4 space-y-2">
                {step.items.map((item) => (
                  <li
                    key={item}
                    className="flex items-start gap-2 text-sm text-neutral-500"
                  >
                    <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-red-500/60" />
                    {item}
                  </li>
                ))}
              </ul>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ─── QUICK START ─── */

function QuickStart() {
  return (
    <section className="py-16 sm:py-24">
      <div className="mx-auto max-w-6xl px-6">
        <div className="grid grid-cols-1 items-start gap-10 lg:grid-cols-2">
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4 }}
          >
            <p className="text-sm font-medium text-red-500/90 tracking-wide">
              Quick start
            </p>
            <h2 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
              Two ways to run.
            </h2>
            <p className="mt-4 text-sm leading-relaxed text-neutral-500">
              <strong className="text-neutral-400">Fastest:</strong> grab a native installer from the{" "}
              <Link href="/download" className="underline decoration-neutral-700 underline-offset-4 transition hover:text-white hover:decoration-neutral-500">download page</Link>
              {" "}— JDK is bundled, nothing else to install.
              <br /><br />
              <strong className="text-neutral-400">From source:</strong> clone the repo and use the Maven wrapper.
              Run <code className="rounded bg-neutral-800/80 px-1.5 py-0.5 text-xs text-neutral-300">./mvnw spring-boot:run</code>{" "}
              to start the full app (backend + JavaFX UI), or{" "}
              <code className="rounded bg-neutral-800/80 px-1.5 py-0.5 text-xs text-neutral-300">./mvnw javafx:run</code>{" "}
              to launch the JavaFX workspace directly.
            </p>
            <div className="mt-8 space-y-3 text-sm text-neutral-500">
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-500/70" />
                JDK 17+ required (bundled in native installers)
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-500/70" />
                Native installers: .dmg · .msi · .deb · .rpm
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-500/70" />
                Two run modes: spring-boot:run · javafx:run
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-500/70" />
                Data stored locally in ~/.intend/ — no cloud
              </div>
            </div>
            <Link
              href="/docs/installation"
              className="mt-6 inline-block text-sm text-neutral-400 underline decoration-neutral-700 underline-offset-4 transition hover:text-white hover:decoration-neutral-500"
            >
              Full installation guide →
            </Link>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4, delay: 0.15 }}
          >
            <Terminal className="w-full max-w-lg">
              <TypingAnimation>
                $ git clone https://github.com/pskuntal1248/http-client-intend.git
              </TypingAnimation>
              <AnimatedSpan delay={2200} className="text-neutral-600">
                Cloning into &apos;http-client-intend&apos;...
              </AnimatedSpan>
              <AnimatedSpan delay={3200} className="text-neutral-400">
                ✓ done
              </AnimatedSpan>
              <TypingAnimation delay={3800}>
                $ cd http-client-intend
              </TypingAnimation>
              <AnimatedSpan delay={4600} className="text-neutral-600">
                # Option A — full app (Spring Boot + JavaFX)
              </AnimatedSpan>
              <TypingAnimation delay={5200}>
                $ ./mvnw spring-boot:run
              </TypingAnimation>
              <AnimatedSpan delay={6400} className="text-neutral-600">
                :: Spring Boot :: (v3.2.2)
              </AnimatedSpan>
              <AnimatedSpan delay={6800} className="text-emerald-500/80">
                ✓ Intend workspace ready — window opened
              </AnimatedSpan>
              <AnimatedSpan delay={7800} className="text-neutral-600">
                {"\n"}# Option B — JavaFX UI only
              </AnimatedSpan>
              <TypingAnimation delay={8400}>
                $ ./mvnw javafx:run
              </TypingAnimation>
              <AnimatedSpan delay={9400} className="text-emerald-500/80">
                ✓ JavaFX workspace launched
              </AnimatedSpan>
            </Terminal>
          </motion.div>
        </div>
      </div>
    </section>
  );
}

/* ─── COMPARISON ─── */

const rows = [
  { feat: "Zero-header requests", intend: true, postman: false, insomnia: false },
  { feat: "Auto Content-Type detection", intend: true, postman: false, insomnia: false },
  { feat: "Stripe-style idempotency + retry", intend: true, postman: false, insomnia: false },
  { feat: "Browser-like headers (bypass WAFs)", intend: true, postman: false, insomnia: false },
  { feat: "Response variable capture", intend: true, postman: false, insomnia: false },
  { feat: "No account required", intend: true, postman: false, insomnia: false },
  { feat: "Native desktop (not Electron)", intend: true, postman: false, insomnia: false },
  { feat: "Open plugin architecture", intend: true, postman: false, insomnia: false },
];

function Comparison() {
  return (
    <section className="border-t border-neutral-800/80 py-16 sm:py-24">
      <div className="mx-auto max-w-3xl px-6">
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4 }}
          className="text-center"
        >
          <p className="text-sm font-medium text-red-500/90 tracking-wide">
            Comparison
          </p>
          <h2 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
            How Intend stacks up.
          </h2>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 10 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4, delay: 0.15 }}
          className="mt-10 overflow-hidden rounded-2xl border border-neutral-800"
        >
          <div className="overflow-x-auto">
            <table className="w-full text-sm min-w-[36rem]">
              <thead>
                <tr className="border-b border-neutral-800 bg-neutral-950/50">
                  <th className="px-5 py-3.5 text-left font-medium text-neutral-600" />
                  <th className="w-24 px-4 py-3.5 text-center font-semibold text-white">
                    Intend
                  </th>
                  <th className="w-24 px-4 py-3.5 text-center font-medium text-neutral-600">
                    Postman
                  </th>
                  <th className="w-24 px-4 py-3.5 text-center font-medium text-neutral-600">
                    Insomnia
                  </th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr
                    key={r.feat}
                    className="border-b border-neutral-800/40 last:border-0"
                  >
                    <td className="px-5 py-3 text-neutral-500">{r.feat}</td>
                    <td className="px-4 py-3 text-center">
                      {r.intend ? (
                        <CheckCircle2 className="mx-auto h-4 w-4 text-emerald-500/80" />
                      ) : (
                        <XCircle className="mx-auto h-4 w-4 text-neutral-800" />
                      )}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <XCircle className="mx-auto h-4 w-4 text-neutral-800" />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <XCircle className="mx-auto h-4 w-4 text-neutral-800" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </motion.div>
      </div>
    </section>
  );
}

/* ─── CTA ─── */

function CTA() {
  return (
    <section className="border-t border-neutral-800/80 py-16 sm:py-24">
      <div className="mx-auto max-w-xl px-6 text-center">
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.4 }}
        >
          <h2 className="text-xl font-bold text-white sm:text-2xl">
            Ready to send your first request?
          </h2>
          <p className="mt-3 text-sm text-neutral-500 sm:text-base">
            Download Intend and start testing APIs in seconds. No sign-up, no
            configuration, no bloat.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
            <Link
              href="/download"
              className="w-48 rounded-lg bg-red-600 px-6 py-2.5 text-center text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-red-500 shadow-[0_0_15px_rgba(220,38,38,0.3)] hover:shadow-[0_0_20px_rgba(220,38,38,0.5)]"
            >
              Download Intend
            </Link>
            <Link
              href="/docs"
              className="w-48 rounded-lg border border-neutral-700 px-6 py-2.5 text-center text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-neutral-900"
            >
              Read the docs
            </Link>
          </div>
        </motion.div>
      </div>
    </section>
  );
}

/* ─── FOOTER ─── */

function Footer() {
  return (
    <footer className="border-t border-neutral-800/80 py-8">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 text-sm text-neutral-600">
        <div className="flex items-center gap-2.5">
          <img src="/logo.png" alt="Intend" className="h-5 w-auto opacity-50" />
          <span>
            Made by{" "}
            <a
              href="https://github.com/pskuntal1248"
              target="_blank"
              rel="noopener noreferrer"
              className="text-neutral-500 transition hover:text-white"
            >
              @pskuntal1248
            </a>
          </span>
        </div>
        <div className="flex items-center gap-5">
          <Link href="/download" className="transition hover:text-neutral-400">
            Download
          </Link>
          <Link href="/docs" className="transition hover:text-neutral-400">
            Docs
          </Link>
          <a
            href="https://github.com/pskuntal1248/http-client-intend"
            target="_blank"
            rel="noopener noreferrer"
            className="transition hover:text-neutral-400"
          >
            GitHub
          </a>
        </div>
      </div>
    </footer>
  );
}

/* ─── PAGE ─── */

export default function Page() {
  return (
    <div className="dark bg-black text-white min-h-[100dvh] w-full selection:bg-neutral-800 selection:text-white">
      <Nav />
      <main>
        <Hero />
        <Features />
        <Pipeline />
        <QuickStart />
        <Comparison />
        <CTA />
      </main>
      <Footer />
    </div>
  );
}
