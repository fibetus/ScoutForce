import type { UiMatch } from "../types/domain";

/**
 * Props for {@link MatchCard}.
 */
export interface MatchCardProps {
  /** Match rendered by this card, including the selected player's box-score. */
  match: UiMatch;
  /** Whether this match is currently selected for the report subset. */
  selected: boolean;
  /** Invoked when the card is toggled (selected/deselected). */
  onToggle: () => void;
}

/**
 * Selectable card summarizing a single match for the selected player.
 *
 * Shows the match date, the final score line with host and guest club names,
 * and a box-score row mapping the player's stats to the MIN/PTS/REB/AST/STL/BLK
 * labels. The displayed values come straight from `match.stats` with no
 * client-side calculation. When `selected`, the card adopts the accented
 * border and tinted background; otherwise it uses the muted default style with
 * a hover affordance and toggles selection via `onToggle`.
 *
 * Note: the prototype's match format badge is intentionally omitted because the
 * backend `Match` entity has no format field, so only fields available in
 * `MatchWithStatsDto` / `UiMatch` are rendered.
 */
export function MatchCard({ match, selected, onToggle }: MatchCardProps) {
  return (
    <button
      onClick={onToggle}
      className={`w-full p-4 rounded-lg border transition-all text-left ${
        selected
          ? "bg-[#FF6A1A]/10 border-[#FF6A1A]"
          : "bg-[#14141A] border-[#26262F] hover:border-[#3A3A45]"
      }`}
    >
      <div className="flex items-start justify-between mb-3">
        <div className="text-sm text-[#6B6B75]">
          {match.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
        </div>
      </div>
      <div className="text-lg font-semibold mb-3" style={{ fontFeatureSettings: '"tnum"' }}>
        {match.host.name} {match.host_score} - {match.guest_score} {match.guest.name}
      </div>
      <div className="flex gap-4 text-sm">
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">MIN</span>
          <span className="font-semibold">{match.stats.minutes_played}</span>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">PTS</span>
          <span className="font-semibold">{match.stats.points}</span>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">REB</span>
          <span className="font-semibold">{match.stats.rebounds}</span>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">AST</span>
          <span className="font-semibold">{match.stats.assists}</span>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">STL</span>
          <span className="font-semibold">{match.stats.steals}</span>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-[#6B6B75]">BLK</span>
          <span className="font-semibold">{match.stats.blocks}</span>
        </div>
      </div>
    </button>
  );
}
