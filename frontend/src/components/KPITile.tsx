/**
 * Props for {@link KPITile}.
 */
export interface KPITileProps {
  /** Short metric label shown beneath the value (e.g. "PPG", "RPG"). */
  label: string;
  /** Pre-formatted metric value to display (e.g. "31.3"). */
  value: string;
}

/**
 * Compact tile displaying a single key performance indicator.
 *
 * Shows a prominent numeric value with a small descriptive label below it.
 * Uses tabular figures (`tnum`) so values align cleanly in a grid of tiles.
 */
export function KPITile({ label, value }: KPITileProps) {
  return (
    <div className="bg-[#1C1C24] rounded-lg p-4 border border-[#26262F]">
      <div className="text-2xl font-bold mb-1" style={{ fontFeatureSettings: '"tnum"' }}>
        {value}
      </div>
      <div className="text-xs text-[#6B6B75]">{label}</div>
    </div>
  );
}
