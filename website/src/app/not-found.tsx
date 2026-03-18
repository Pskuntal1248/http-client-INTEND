"use client";

import { motion } from "motion/react";
import { ArrowLeft } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-black px-6">
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="text-center"
      >
        <p className="font-mono text-7xl font-bold text-neutral-800 sm:text-9xl">
          404
        </p>
        <h1 className="mt-4 text-xl font-semibold text-white sm:text-2xl">
          Page not found
        </h1>
        <p className="mt-2 text-sm text-neutral-500 sm:text-base">
          The page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
          <a
            href="/"
            className="inline-flex items-center gap-2 rounded-lg bg-white px-5 py-2.5 text-sm font-medium text-black transition hover:-translate-y-0.5 hover:bg-neutral-200"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to home
          </a>
          <a
            href="/docs"
            className="rounded-lg border border-neutral-700 px-5 py-2.5 text-sm font-medium text-white transition hover:-translate-y-0.5 hover:bg-neutral-900"
          >
            Read the docs
          </a>
        </div>
      </motion.div>
    </div>
  );
}
