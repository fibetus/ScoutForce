import type { ReactNode } from "react";

/**
 * Props for {@link EmptyState}.
 */
export interface EmptyStateProps {
  /** Decorative icon or emoji shown above the title. */
  icon: ReactNode;
  /** Primary heading describing the empty condition. */
  title: string;
  /** Supporting text guiding the user on what to do next. */
  description: string;
}

/**
 * Centered placeholder shown when a view has no content to display.
 *
 * Renders a large icon, a heading and a muted description, vertically and
 * horizontally centered within the available space.
 */
export function EmptyState({ icon, title, description }: EmptyStateProps) {
  return (
    <div className="flex items-center justify-center h-full p-8">
      <div className="text-center max-w-md">
        <div className="text-6xl mb-4">{icon}</div>
        <h3 className="text-xl font-semibold mb-2">{title}</h3>
        <p className="text-[#6B6B75]">{description}</p>
      </div>
    </div>
  );
}
