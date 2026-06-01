import { CheckCircle2 } from "lucide-react";
import type { UiPlayer, UiReportResult } from "../types/domain";

/**
 * Props for {@link ReportSuccessView}.
 */
export interface ReportSuccessViewProps {
  /** Player the saved report belongs to, used for the confirmation message. */
  player: UiPlayer;
  /**
   * Saved report result returned by the backend.
   *
   * Its `finalRating` is the authoritative value displayed on screen and
   * replaces any client-side preview computed while editing the form.
   */
  result: UiReportResult;
  /** Invoked when the user chooses to return to the player detail view. */
  onBackToPlayer: () => void;
}

/**
 * Success screen shown after a scouting report has been saved.
 *
 * Confirms the report submission for the selected player and presents the
 * authoritative `finalRating` (formatted to two decimals, out of 10) together
 * with the chosen recommendation, color-coded by recommendation type. The
 * displayed `finalRating` and `recommendation` come from the backend-returned
 * {@link UiReportResult} (the source of truth), not from the client-side
 * preview. Two actions are offered: returning to the player detail view and
 * starting a fresh report by reloading the application.
 */
export function ReportSuccessView({ player, result, onBackToPlayer }: ReportSuccessViewProps) {
  const { finalRating, recommendation } = result;

  return (
    <div className="p-8 flex items-center justify-center min-h-full">
      <div className="max-w-2xl w-full text-center space-y-6">
        <div className="w-16 h-16 rounded-full bg-[#2EBD85]/20 flex items-center justify-center mx-auto mb-4">
          <CheckCircle2 className="w-8 h-8 text-[#2EBD85]" />
        </div>
        <h2 className="text-3xl font-bold">Report Submitted Successfully</h2>
        <p className="text-[#A1A1AA]">
          Scouting report for {player.firstName} {player.lastName} has been saved
        </p>

        <div className="bg-[#14141A] rounded-2xl p-8 border border-[#26262F] space-y-6">
          <div>
            <div className="text-sm text-[#6B6B75] mb-2">Final Rating</div>
            <div className="text-6xl font-bold" style={{ fontFeatureSettings: '"tnum"' }}>
              {finalRating.toFixed(2)}
            </div>
            <div className="text-[#6B6B75] mt-2">out of 10</div>
          </div>

          <div>
            <div className="text-sm text-[#6B6B75] mb-2">Recommendation</div>
            <div className={`inline-block px-6 py-3 rounded-lg font-semibold ${
              recommendation === 'strong_buy' ? 'bg-[#2EBD85]/20 text-[#2EBD85]' :
              recommendation === 'buy' ? 'bg-[#FF6A1A]/20 text-[#FF6A1A]' :
              recommendation === 'neutral' ? 'bg-[#6B6B75]/20 text-[#A1A1AA]' :
              'bg-[#E5484D]/20 text-[#E5484D]'
            }`}>
              {(recommendation ?? '').toUpperCase().replace('_', ' ')}
            </div>
          </div>
        </div>

        <div className="flex gap-3 justify-center">
          <button
            onClick={onBackToPlayer}
            className="px-6 py-3 bg-[#FF6A1A] hover:bg-[#FF8033] text-white rounded-lg font-medium transition-colors"
          >
            Back to Player
          </button>
          <button
            onClick={() => window.location.reload()}
            className="px-6 py-3 bg-transparent border-2 border-[#3A3A45] hover:border-[#FF6A1A] text-[#F5F5F7] rounded-lg font-medium transition-colors"
          >
            Create Another Report
          </button>
        </div>
      </div>
    </div>
  );
}
