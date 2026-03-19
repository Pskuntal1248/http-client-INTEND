"use client";

import { Download, Github, ArrowLeft } from "lucide-react";
import { motion } from "motion/react";
import Link from "next/link";

/* ─── DOWNLOAD DATA ─── */

const platforms = [
  {
    os: "macOS",
    description: "Universal binary for Apple Silicon & Intel",
    builds: [
      {
        label: "Intend-2.0.0.dmg",
        format: ".dmg",
        url: "https://github.com/Pskuntal1248/http-client-INTEND/releases/download/v2.0.0/Intend-2.0.0.dmg",
      },
    ],
    // icon placeholder – replace with your own SVG / image
    iconSlot: "macos",
  },
  {
    os: "Windows",
    description: "64-bit installer for Windows 10+",
    builds: [
      {
        label: "Intend-2.0.0.msi",
        format: ".msi",
        url: "https://github.com/Pskuntal1248/http-client-INTEND/releases/download/v2.0.0/Intend-2.0.0.msi",
      },
      {
        label: "Intend-2.0.0.exe",
        format: ".exe",
        url: "https://github.com/Pskuntal1248/http-client-INTEND/releases/download/v2.0.0/Intend-2.0.0.exe",
      },
    ],
    iconSlot: "windows",
  },
  {
    os: "Linux",
    description: "Packages for Debian & RPM-based distros",
    builds: [
      {
        label: "intend_2.0.0-1_amd64.deb",
        format: ".deb",
        url: "https://github.com/Pskuntal1248/http-client-INTEND/releases/download/v2.0.0/intend_2.0.0-1_amd64.deb",
      },
      {
        label: "intend-2.0.0-1.x86_64.rpm",
        format: ".rpm",
        url: "https://github.com/Pskuntal1248/http-client-INTEND/releases/download/v2.0.0/intend-2.0.0-1.x86_64.rpm",
      },
    ],
    iconSlot: "linux",
  },
];

/* ─── ICON PLACEHOLDERS ─── */
/* Replace these SVGs with your own icons/images for each OS */

function OsIcon({ slot }: { slot: string }) {
  // Placeholder icons – simple monochrome glyphs
  const icons: Record<string, React.ReactNode> = {
    macos: (
      /* Apple-style placeholder */
      <svg viewBox="0 0 24 24" fill="currentColor" className="h-8 w-8">
        <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" />
      </svg>
    ),
    windows: (
      /* Windows-style placeholder */
      <svg viewBox="0 0 24 24" fill="currentColor" className="h-8 w-8">
        <path d="M3 12V6.5l8-1.1V12H3zm0 .5h8v6.6l-8-1.1V12.5zM11.5 5.3l9.5-1.3v8h-9.5V5.3zm0 7.2h9.5v8l-9.5-1.3v-6.7z" />
      </svg>
    ),
    linux: (
      /* Linux/Tux-style placeholder */
      <svg viewBox="0 0 24 24" fill="currentColor" className="h-8 w-8">
        <path d="M12.504 0c-.155 0-.315.008-.48.021-4.226.333-3.105 4.807-3.17 6.298-.076 1.092-.3 1.953-1.05 3.02-.885 1.051-2.127 2.75-2.716 4.521-.278.832-.41 1.684-.287 2.489a.424.424 0 00-.11.135c-.26.268-.45.6-.663.839-.199.199-.485.267-.797.4-.313.136-.658.269-.864.68-.09.189-.136.394-.132.602 0 .199.027.4.055.536.058.399.116.728.04.97-.249.68-.28 1.145-.106 1.484.174.334.535.47.94.601.81.2 1.91.135 2.774.6.926.466 1.866.67 2.616.47.526-.116.97-.464 1.208-.946.587.26 1.22.428 1.768.332.55-.093.896-.462 1.056-.863.42.509.922.772 1.394.723.57-.065.462-.386.462-.386-.682.124-1.09-.313-1.487-.756l-.01-.01c.397-.397.665-.873.787-1.404.178-.56.038-.55.467-.92.34-.328.273-.709-.066-1.289a5.278 5.278 0 00-.157-.233c.077-.072.098-.151.1-.255a11.597 11.597 0 00-.019-1.048c-.01-.146-.005-.313.065-.467.161-.333.203-.66.134-.981-.063-.292-.2-.587-.372-.838.144-.186.255-.39.298-.628.06-.368-.057-.636-.262-.812.065-.203.043-.447-.066-.706a1.71 1.71 0 00-.375-.537c.05-.07.097-.163.097-.313 0-.092-.005-.202-.059-.335-.054-.134-.18-.297-.345-.297-.096 0-.219.072-.297.143a6.853 6.853 0 00-.178-.143c.063.063.089.133.089.2 0 .1-.055.181-.073.217a2.08 2.08 0 01-.235.122c-.093.038-.189.08-.249.147a.37.37 0 00-.088.258c0 .092.031.136.07.189a3.08 3.08 0 01.256.392c.055.105.094.216.087.368 0 .199-.119.39-.285.496.069.143.118.29.135.467.024.243-.037.439-.137.594.2.176.324.4.361.653.037.267-.019.489-.112.665a5.3 5.3 0 01.176.263c.263.455.264.661.122.8-.256.25-.54.12-.633.093l-.021-.007c-.002-.001-.002.003-.002.003a2.197 2.197 0 01-.041.652c-.095.418-.3.752-.582 1.027.236.381.372.833.297 1.264-.106.603-.547 1.068-1.152 1.265-.604.199-1.378.137-2.17-.285-.65-.349-1.655-.41-2.382-.583-.364-.115-.507-.166-.59-.334-.085-.173-.067-.453.186-1.025.123-.275.065-.628.003-1.056a5.21 5.21 0 01-.053-.494c0-.097.01-.17.074-.27.152-.208.437-.353.737-.53.252-.144.5-.303.67-.537a.89.89 0 00.133-.49c0-.265-.077-.437-.173-.55a.998.998 0 00-.244-.19c-.065-.035-.132-.052-.176-.052l-.25.05c.244-.223.485-.509.577-.848.023-.085.035-.175.035-.266 0-.279-.109-.52-.277-.688-.172-.169-.398-.268-.591-.268-.044 0-.086.005-.124.018-.238.084-.367.334-.415.568a2.15 2.15 0 00-.032.493c-.03.163-.112.3-.243.375-.134.072-.308.1-.568.16-.27.068-.646.15-.822.465a1.05 1.05 0 00-.113.479c0 .24.068.444.166.603a6.15 6.15 0 01-.42.768c-.557 1.054-1.02 2.097-1.014 3.245 0 .402.063.81.196 1.198-.39-.08-.803-.224-1.116-.393-.574-.298-.94-.683-.94-1.264 0-.095.006-.155.006-.22.011-.156.018-.322-.093-.579-.117-.27-.394-.418-.792-.418-.24 0-.377.069-.505.158-.13.09-.227.208-.318.297-.183.178-.323.283-.592.283-.088 0-.184-.036-.277-.085.266-.199.36-.56.257-.87a.84.84 0 00-.296-.398.85.85 0 00-.485-.155c-.227 0-.435.078-.587.252-.152.178-.218.414-.218.677 0 .164.029.307.069.423-.155.037-.344.088-.485.212a.757.757 0 00-.238.446c-.02.127-.008.254.027.378.055.2.17.37.293.472.122.1.245.148.345.148h.09c-.035.162-.06.342-.06.523 0 .392.092.75.251 1.04-.108.102-.174.24-.174.398 0 .202.109.336.232.416.123.077.266.116.4.116.277 0 .49-.13.585-.255a.817.817 0 00.14-.454c0-.088-.01-.177-.055-.255a.614.614 0 00-.168-.196c.088-.122.15-.268.183-.424.032-.155.032-.323.007-.49.237.084.486.131.733.131.24 0 .477-.049.678-.164a.96.96 0 00.289-.235c-.038.33-.01.666.09.976.143.462.42.832.764 1.05a1.57 1.57 0 01-.183.052c-.28.05-.53.073-.735.03a.706.706 0 01-.44-.28.42.42 0 00-.352-.188.42.42 0 00-.352.741c.201.245.475.401.791.472.317.073.651.05.984-.01.336-.07.662-.195.895-.407a.906.906 0 00.309-.599c.33.176.59.175.764.086.227-.116.298-.376.298-.582 0-.065-.004-.128-.013-.187z" />
      </svg>
    ),
  };

  return (
    <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-neutral-800 bg-neutral-900/50 text-neutral-400">
      {icons[slot] ?? <Download className="h-7 w-7" />}
    </div>
  );
}

/* ─── NAV (reused from main page) ─── */

function Nav() {
  return (
    <nav className="sticky top-0 z-50 border-b border-neutral-800/80 bg-black/70 backdrop-blur-xl">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-6">
        <div className="flex items-center gap-2.5">
          <img src="/logo.png" alt="Intend" className="h-7 w-auto" />
          <Link href="/" className="text-base font-bold text-white">
            Intend
          </Link>
        </div>
        <div className="hidden items-center gap-6 text-sm text-neutral-500 md:flex">
          <Link href="/#features" className="transition hover:text-neutral-300">
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
        <Link
          href="/download"
          className="rounded-lg bg-red-600 px-4 py-1.5 text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-red-500 shadow-[0_0_15px_rgba(220,38,38,0.3)] hover:shadow-[0_0_20px_rgba(220,38,38,0.5)]"
        >
          Download
        </Link>
      </div>
    </nav>
  );
}

/* ─── FOOTER ─── */

function Footer() {
  return (
    <footer className="border-t border-neutral-800/80 py-8">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 text-sm text-neutral-600">
        <div className="flex items-center gap-2.5">
          <img
            src="/logo.png"
            alt="Intend"
            className="h-5 w-auto opacity-50"
          />
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

export default function DownloadPage() {
  return (
    <div className="dark bg-black text-white min-h-[100dvh] w-full selection:bg-neutral-800 selection:text-white">
      <Nav />
      <main className="mx-auto max-w-4xl px-6 py-16 sm:py-24">
        {/* Back link */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.2 }}
        >
          <Link
            href="/"
            className="inline-flex items-center gap-1.5 text-sm text-neutral-600 transition hover:text-neutral-400"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Back to home
          </Link>
        </motion.div>

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, delay: 0.05 }}
          className="mt-8"
        >
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold text-white sm:text-4xl">
              Download Intend
            </h1>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src="https://img.shields.io/github/v/release/pskuntal1248/http-client-intend?style=flat&color=dc2626&labelColor=18181b&label=latest"
              alt="Latest version"
              className="h-5 mt-1"
            />
          </div>
          <p className="mt-3 max-w-lg text-base text-neutral-500">
            Native desktop app. No Electron, no account, fully
            offline. Pick your platform below.
          </p>
        </motion.div>

        {/* Platform cards */}
        <div className="mt-12 space-y-5">
          {platforms.map((p, i) => (
            <motion.div
              key={p.os}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.35, delay: 0.12 + i * 0.08 }}
              className="rounded-2xl border border-neutral-800 bg-neutral-950 p-6"
            >
              <div className="flex items-start gap-5">
                {/* OS icon placeholder */}
                <OsIcon slot={p.iconSlot} />

                <div className="flex-1">
                  <h2 className="text-lg font-semibold text-white">{p.os}</h2>
                  <p className="mt-0.5 text-sm text-neutral-500">
                    {p.description}
                  </p>

                  {/* Download buttons */}
                  <div className="mt-4 flex flex-wrap gap-3">
                    {p.builds.map((b) => (
                      <a
                        key={b.format}
                        href={b.url}
                        className="group inline-flex items-center gap-2 rounded-lg border border-neutral-700/80 bg-neutral-900/60 px-4 py-2.5 text-sm font-medium text-neutral-300 transition hover:-translate-y-0.5 hover:border-neutral-600 hover:bg-neutral-800/80 hover:text-white"
                      >
                        <Download className="h-4 w-4 text-neutral-500 transition group-hover:text-white" />
                        <span>{b.label}</span>
                        <span className="ml-1 rounded bg-neutral-800 px-1.5 py-0.5 font-mono text-[11px] text-neutral-500 transition group-hover:bg-neutral-700 group-hover:text-neutral-300">
                          {b.format}
                        </span>
                      </a>
                    ))}
                  </div>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Requirements notice */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3, delay: 0.5 }}
          className="mt-10 rounded-xl border border-neutral-800/60 bg-neutral-950/50 px-5 py-4 text-sm text-neutral-500"
        >
          <p>
            <span className="font-medium text-neutral-400">
              System requirements:
            </span>{" "}
            JDK 17+ is bundled in native installers. No additional dependencies
            needed.
          </p>
        </motion.div>

        {/* Source link */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3, delay: 0.6 }}
          className="mt-6 flex items-center gap-4 text-sm text-neutral-600"
        >
          <a
            href="https://github.com/Pskuntal1248/http-client-INTEND/releases/tag/v2.0.0"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 transition hover:text-neutral-400"
          >
            <Github className="h-4 w-4" />
            View all releases on GitHub
          </a>
        </motion.div>
      </main>
      <Footer />
    </div>
  );
}
