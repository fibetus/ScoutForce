/**
 * Props for {@link NavItem}.
 */
export interface NavItemProps {
  /** Text label rendered inside the navigation button. */
  label: string;
  /** Whether this item represents the currently active route/section. */
  active?: boolean;
}

/**
 * Single sidebar navigation entry rendered as a button.
 *
 * When `active` is true the item is highlighted with the accent color and a
 * filled background; otherwise it uses a muted style with hover affordances.
 */
export function NavItem({ label, active = false }: NavItemProps) {
  return (
    <button
      className={`w-full text-left px-3 py-2 rounded-md text-sm transition-colors ${
        active
          ? "bg-[#1C1C24] text-[#FF6A1A] font-medium"
          : "text-[#A1A1AA] hover:bg-[#1C1C24] hover:text-[#F5F5F7]"
      }`}
    >
      {label}
    </button>
  );
}
