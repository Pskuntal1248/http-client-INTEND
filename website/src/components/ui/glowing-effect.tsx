"use client";

import { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

interface GlowingEffectProps {
  spread?: number;
  glow?: boolean;
  disabled?: boolean;
  proximity?: number;
  inactiveZone?: number;
  borderWidth?: number;
  className?: string;
}

export const GlowingEffect = ({
  spread = 40,
  glow = true,
  disabled = false,
  proximity = 64,
  inactiveZone = 0.01,
  borderWidth = 1,
  className,
}: GlowingEffectProps) => {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (disabled) return;
    const container = containerRef.current;
    if (!container) return;
    const parent = container.parentElement;
    if (!parent) return;

    const handleMove = (e: MouseEvent) => {
      const rect = parent.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      const centerX = rect.width / 2;
      const centerY = rect.height / 2;
      const distFromCenter = Math.sqrt(
        Math.pow((x - centerX) / centerX, 2) +
          Math.pow((y - centerY) / centerY, 2)
      );

      if (distFromCenter < inactiveZone) {
        container.style.opacity = "0";
        return;
      }

      const isNearby =
        x >= -proximity &&
        x <= rect.width + proximity &&
        y >= -proximity &&
        y <= rect.height + proximity;

      if (isNearby) {
        container.style.opacity = "1";
        container.style.setProperty("--glow-x", `${x}px`);
        container.style.setProperty("--glow-y", `${y}px`);
      } else {
        container.style.opacity = "0";
      }
    };

    const handleLeave = () => {
      container.style.opacity = "0";
    };

    document.addEventListener("mousemove", handleMove);
    parent.addEventListener("mouseleave", handleLeave);

    return () => {
      document.removeEventListener("mousemove", handleMove);
      parent.removeEventListener("mouseleave", handleLeave);
    };
  }, [disabled, proximity, inactiveZone]);

  if (disabled) return null;

  return (
    <div
      ref={containerRef}
      className={cn(
        "pointer-events-none absolute -inset-px rounded-[inherit] opacity-0 transition-opacity duration-300",
        className
      )}
      style={
        {
          background: glow
            ? `radial-gradient(${spread * 2}px circle at var(--glow-x) var(--glow-y), rgba(220,38,38,0.12), transparent 70%)`
            : "none",
          border: `${borderWidth}px solid transparent`,
          borderImage: `radial-gradient(${spread * 1.5}px circle at var(--glow-x) var(--glow-y), rgba(220,38,38,0.4), rgba(255,255,255,0.06) 60%, transparent 80%) 1`,
        } as React.CSSProperties
      }
    />
  );
};
