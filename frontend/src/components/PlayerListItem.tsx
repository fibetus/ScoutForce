import type { UiPlayer } from "../types/domain";

/**
 * Props for {@link PlayerListItem}.
 */
export interface PlayerListItemProps {
  /** Player rendered by this list item. */
  player: UiPlayer;
  /** Whether this item is the currently selected player. */
  selected: boolean;
  /** Invoked when the item is activated. */
  onClick: () => void;
}

/**
 * List item for a single player in the players pane (Pane A).
 *
 * Renders the player's initials avatar, name, position, club, and a status
 * pill colored via the `statusColors` map. The status key comes from the
 * adapter as a lowercase enum (`player_status`) and is humanized for display
 * by replacing underscores with spaces. A ten-segment bar visualizes the
 * player's average rating. When `selected`, the item adopts the accented
 * border and tinted background; otherwise it uses the muted default style
 * with a hover affordance.
 */
export function PlayerListItem({ player, selected, onClick }: PlayerListItemProps) {
  const statusColors: Record<string, string> = {
    new: "bg-[#4D8DF7]/10 text-[#4D8DF7]",
    observed: "bg-[#FF6A1A]/10 text-[#FF6A1A]",
    medical_verification: "bg-[#F4B740]/10 text-[#F4B740]",
    invited_to_workout: "bg-[#2EBD85]/10 text-[#2EBD85]",
    invited_to_big_board: "bg-[#2EBD85]/20 text-[#2EBD85]",
    delisted: "bg-[#E5484D]/10 text-[#E5484D]"
  };

  return (
    <button
      onClick={onClick}
      className={`w-full p-4 rounded-lg border transition-all text-left ${
        selected
          ? "bg-[#FF6A1A]/10 border-[#FF6A1A] border-l-2"
          : "bg-[#14141A] border-[#26262F] hover:border-[#3A3A45]"
      }`}
    >
      <div className="flex items-start gap-3">
        <div className="w-10 h-10 rounded-full bg-[#FF6A1A] flex items-center justify-center font-semibold text-sm flex-shrink-0">
          {player.firstName[0]}{player.lastName[0]}
        </div>
        <div className="flex-1 min-w-0">
          <div className="font-semibold mb-1">
            {player.firstName} {player.lastName}
          </div>
          <div className="flex items-center gap-2 mb-2 flex-wrap">
            <span className="px-2 py-0.5 bg-[#1C1C24] text-[#A1A1AA] text-xs rounded-full">
              {player.position}
            </span>
            <span className="text-xs text-[#6B6B75]">{player.club.name}</span>
            <span className={`px-2 py-0.5 text-xs rounded-full ${statusColors[player.player_status]}`}>
              {player.player_status.replace(/_/g, ' ')}
            </span>
          </div>
          <div className="flex items-center gap-1">
            {[...Array(10)].map((_, i) => (
              <div
                key={i}
                className={`h-1.5 w-full rounded-full ${
                  i < Math.floor(player.averageRating)
                    ? "bg-[#FF6A1A]"
                    : "bg-[#26262F]"
                }`}
              />
            ))}
          </div>
          <div className="text-xs text-[#6B6B75] mt-1">{player.averageRating.toFixed(1)} avg</div>
        </div>
      </div>
    </button>
  );
}
