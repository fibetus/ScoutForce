import { ChevronLeft } from "lucide-react";
import type { UiMatch, UiPlayer } from "../types/domain";
import { MatchCard } from "./MatchCard";

/**
 * Props for {@link PlayerMatchesView}.
 */
export interface PlayerMatchesViewProps {
  /** The player whose observed matches are listed. */
  player: UiPlayer;
  /** Matches available for the player; each carries that player's box-score. */
  matches: UiMatch[];
  /** Identifiers of the matches currently selected for the report subset. */
  selectedMatches: number[];
  /** Invoked with a match id when a match card is toggled on or off. */
  onToggleMatch: (id: number) => void;
  /** Invoked to proceed with a report built from the selected matches. */
  onCreateFromSelected: () => void;
  /** Invoked to navigate back to the previous (player detail) view. */
  onBack: () => void;
}

/**
 * Matches view listing the selected player's observed matches.
 *
 * Renders a breadcrumb, then either an empty state or a selectable list of
 * {@link MatchCard} entries. The empty state corresponds to the backend's
 * 204 No Content / E1 case (the scout has not observed any of this player's
 * matches) and offers a way back to the players list. When matches exist, a
 * toolbar reports how many are selected and exposes the "Create Report from
 * Selected" action (disabled until at least one match is selected). Match
 * selection is owned by the parent: this component reflects `selectedMatches`
 * and reports toggles through `onToggleMatch`, while navigation and the
 * proceed action are delegated to `onBack` and `onCreateFromSelected`.
 *
 * All match data is passed in from `App.tsx`; this component performs no data
 * fetching or domain calculations of its own.
 */
export function PlayerMatchesView({ player, matches, selectedMatches, onToggleMatch, onCreateFromSelected, onBack }: PlayerMatchesViewProps) {
  // Filter matches for the selected player only (association-based filtering)
  const playerMatches = matches.filter((m) => m.playerId === player.id);

  return (
    <div className="p-8 space-y-6">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm text-[#6B6B75]">
        <button onClick={onBack} className="hover:text-[#FF6A1A] flex items-center gap-1">
          <ChevronLeft className="w-4 h-4" />
          Players
        </button>
        <span>›</span>
        <span className="text-[#F5F5F7]">{player.firstName} {player.lastName}</span>
        <span>›</span>
        <span className="text-[#F5F5F7]">Matches</span>
      </div>

      {playerMatches.length === 0 ? (
        /* E1 Empty State */
        <div className="flex items-center justify-center h-96">
          <div className="text-center max-w-md">
            <div className="text-6xl mb-4">🏀</div>
            <h3 className="text-xl font-semibold mb-2 text-[#F5F5F7]">No Matches Observed</h3>
            <p className="text-[#6B6B75] mb-4">
              You haven't observed any of {player.firstName} {player.lastName}'s matches yet.
            </p>
            <button
              onClick={onBack}
              className="px-4 py-2 bg-[#FF6A1A] hover:bg-[#FF8033] text-white rounded-lg font-medium transition-colors"
            >
              Back to Players
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Toolbar */}
          <div className="bg-[#14141A] rounded-lg p-4 border border-[#26262F] flex items-center justify-between">
            <div className="text-sm text-[#A1A1AA]">
              {selectedMatches.length} match{selectedMatches.length !== 1 ? 'es' : ''} selected
            </div>
            <button
              onClick={onCreateFromSelected}
              disabled={selectedMatches.length === 0}
              className={`px-4 py-2 rounded-lg font-medium text-sm transition-colors ${
                selectedMatches.length > 0
                  ? "bg-[#FF6A1A] hover:bg-[#FF8033] text-white"
                  : "bg-[#26262F] text-[#6B6B75] cursor-not-allowed"
              }`}
            >
              Create Report from Selected
            </button>
          </div>

          {/* Matches Grid */}
          <div className="space-y-3">
            {playerMatches.map((match) => (
              <MatchCard
                key={match.id}
                match={match}
                selected={selectedMatches.includes(match.id)}
                onToggle={() => onToggleMatch(match.id)}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
