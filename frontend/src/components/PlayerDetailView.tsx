import { AlertCircle, ArrowRight } from "lucide-react";
import type { UiPlayer } from "../types/domain";
import { KPITile } from "./KPITile";

/**
 * Props for {@link PlayerDetailView}.
 */
export interface PlayerDetailViewProps {
  /** The player whose details, KPIs and physical measurements are shown. */
  player: UiPlayer;
  /** Invoked when the scout activates the "Create Scouting Report" action. */
  onCreateReport: () => void;
  /** Invoked when the scout activates the "View Player Matches" action. */
  onViewMatches: () => void;
  /**
   * Controls visibility of the E1 error banner.
   *
   * The original prototype computed this locally from mock data; in the ported
   * application the E1 condition is determined by the backend (HTTP 422) and
   * surfaced by the orchestrating container, so it is passed in as a prop.
   */
  showE1Error?: boolean;
}

/**
 * Player detail screen showing the hero header, KPI tiles and primary actions.
 *
 * Renders the selected player's name, position, club and physical measurements
 * (height and weight in metric units), the average rating badge, four KPI tiles
 * filled from backend-provided per-game averages (formatted to one decimal
 * place), and the create-report / view-matches actions. When {@link
 * PlayerDetailViewProps.showE1Error} is set, the E1 banner explains that the
 * player has no observed matches and a report cannot be created.
 */
export function PlayerDetailView({ player, onCreateReport, onViewMatches, showE1Error = false }: PlayerDetailViewProps) {
  return (
    <div className="p-8 space-y-6">
      {/* E1 Error Banner */}
      {showE1Error && (
        <div className="bg-[#E5484D]/10 border border-[#E5484D] rounded-lg p-4">
          <div className="flex items-start gap-2">
            <AlertCircle className="w-5 h-5 text-[#E5484D] flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <div className="font-semibold text-[#E5484D] mb-1">Cannot Create Report</div>
              <p className="text-sm text-[#E5484D]">
                E1: The selected player has no matches that you have observed. Please observe at least one match before creating a report.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Hero Header */}
      <div className="bg-[#14141A] rounded-2xl p-8 border border-[#26262F]">
        <div className="flex items-start justify-between mb-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">
              {player.firstName} {player.lastName}
            </h1>
            <div className="flex items-center gap-3 text-[#A1A1AA]">
              <span className="px-3 py-1 bg-[#1C1C24] rounded-full">{player.position}</span>
              <span>{player.club.name}</span>
              <span>•</span>
              <span>{player.height} cm</span>
              <span>•</span>
              <span>{player.weight} kg</span>
            </div>
          </div>
          <div className="text-center">
            <div className="w-24 h-24 rounded-full border-4 border-[#FF6A1A] flex items-center justify-center mb-2">
              <div className="text-3xl font-bold" style={{ fontFeatureSettings: '"tnum"' }}>
                {player.averageRating.toFixed(1)}
              </div>
            </div>
            <div className="text-xs text-[#6B6B75]">Avg Rating</div>
          </div>
        </div>

        {/* KPI Tiles */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <KPITile label="PPG" value={player.kpi.ppg.toFixed(1)} />
          <KPITile label="RPG" value={player.kpi.rpg.toFixed(1)} />
          <KPITile label="APG" value={player.kpi.apg.toFixed(1)} />
          <KPITile label="MPG" value={player.kpi.mpg.toFixed(1)} />
        </div>

        {/* Primary Actions */}
        <div className="flex gap-3">
          <button
            onClick={onCreateReport}
            className="flex-1 bg-[#FF6A1A] hover:bg-[#FF8033] text-white px-6 py-3 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors"
          >
            Create Scouting Report
            <ArrowRight className="w-5 h-5" />
          </button>
          <button
            onClick={onViewMatches}
            className="flex-1 bg-transparent border-2 border-[#3A3A45] hover:border-[#FF6A1A] text-[#F5F5F7] px-6 py-3 rounded-lg font-medium transition-colors"
          >
            View Player Matches
          </button>
        </div>
      </div>
    </div>
  );
}
