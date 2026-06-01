/**
 * Props for {@link StatChip}.
 */
export interface StatChipProps {
  /** Muted label describing the statistic. */
  label: string;
  /** Pre-formatted statistic value displayed in bold. */
  value: string;
}

/**
 * Pill-shaped chip pairing a muted label with a bold statistic value.
 *
 * Uses tabular figures (`tnum`) so the value stays aligned across chips.
 */
export function StatChip({ label, value }: StatChipProps) {
  return (
    <div className="px-3 py-1.5 bg-[#1C1C24] rounded-full text-sm">
      <span className="text-[#6B6B75]">{label}</span>
      <span className="ml-2 font-semibold" style={{ fontFeatureSettings: '"tnum"' }}>{value}</span>
    </div>
  );
}
