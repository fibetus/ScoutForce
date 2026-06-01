import { useState } from "react";
import { AlertCircle, Plus } from "lucide-react";
import { StatChip } from "./StatChip";
import { DetailedRatingRow } from "./DetailedRatingRow";
import { RecommendationCard } from "./RecommendationCard";
import type {
  UiPlayer,
  UiMatch,
  UiDetailedRating,
  Recommendation,
} from "../types/domain";

/**
 * Assembled report form data emitted to the parent on a successful submit.
 *
 * This is the transient client-side form state (Report_Form_Data). The parent
 * (App.tsx) is responsible for translating it into the backend payload and
 * issuing the network request; the final rating shown after saving comes from
 * the backend response, not from the client-side preview computed here.
 */
export interface CreateReportFormData {
  /** Detailed ratings entered by the scout. */
  detailedRatings: UiDetailedRating[];
  /** Free-text report note. */
  note: string;
  /** Selected draft recommendation. */
  recommendation: Recommendation;
}

/**
 * Props for {@link CreateReportView}.
 */
export interface CreateReportViewProps {
  /** Player the report is being written for. */
  player: UiPlayer;
  /** Matches in scope for this report, used to render the Match Scope list. */
  matches: UiMatch[];
  /**
   * Whether the scout explicitly selected a subset of matches (the A1 flow).
   *
   * Drives visibility of the averages panel: in the default flow (all observed
   * matches) the panel shows the backend aggregate `player.kpi`; in the A1 flow
   * the panel is hidden because the backend aggregate would not match the
   * selected subset and the frontend must not recompute domain statistics.
   */
  matchSubsetSelected: boolean;
  /**
   * Invoked with the assembled form data once client-side validation passes.
   * The parent performs the actual submit/network call.
   */
  onSubmit: (formData: CreateReportFormData) => void;
  /** Invoked when the scout cancels report creation. */
  onCancel: () => void;
}

/**
 * Computes the sum of the weights across all detailed ratings.
 *
 * Operates purely on Report_Form_Data and performs no domain-data lookups.
 *
 * @param detailedRatings - The detailed rating rows of the form.
 * @returns The arithmetic sum of every rating's weight.
 */
function calculateWeightSum(detailedRatings: UiDetailedRating[]): number {
  return detailedRatings.reduce((sum, r) => sum + r.weight, 0);
}

/**
 * Computes the client-side preview of the final rating as the weighted sum of
 * the detailed ratings (rating × weight), rounded to two decimal places.
 *
 * This is a preview only; after the report is saved the authoritative value is
 * the `finalRating` returned by the backend.
 *
 * @param detailedRatings - The detailed rating rows of the form.
 * @returns The previewed final rating, or `0` when there are no ratings.
 */
function calculateFinalRating(detailedRatings: UiDetailedRating[]): number {
  if (detailedRatings.length === 0) return 0;
  const sum = detailedRatings.reduce((acc, r) => acc + r.rating * r.weight, 0);
  return parseFloat(sum.toFixed(2));
}

/**
 * Distributes weights evenly across the detailed ratings so that they sum to
 * exactly `1.0`, returning a new array (the input is left unchanged).
 *
 * @param detailedRatings - The detailed rating rows of the form.
 * @returns A new array with balanced weights, or the input array when empty.
 */
function autoBalanceWeights(detailedRatings: UiDetailedRating[]): UiDetailedRating[] {
  if (detailedRatings.length === 0) return detailedRatings;
  const equalWeight = 1.0 / detailedRatings.length;
  return detailedRatings.map((r) => ({ ...r, weight: parseFloat(equalWeight.toFixed(3)) }));
}

/**
 * Create-report view: the scouting report form plus a sticky live-preview
 * summary, ported 1:1 from the prototype.
 *
 * Owns the transient form state (detailed ratings, note, recommendation) and
 * the client-side calculations on it ({@link calculateWeightSum},
 * {@link autoBalanceWeights}, {@link calculateFinalRating} preview). On submit
 * it runs the E2–E5 client-side validation; if any rule is violated it blocks
 * the submit and lists the messages in `validationErrors`, otherwise it emits
 * the assembled form data through `onSubmit` for the parent to send.
 *
 * The Match Scope card shows the matches in scope; its averages panel displays
 * the backend aggregate `player.kpi` in the default flow and is hidden in the
 * A1 subset flow (see {@link CreateReportViewProps.matchSubsetSelected}).
 */
export function CreateReportView({
  player,
  matches,
  matchSubsetSelected,
  onSubmit,
  onCancel,
}: CreateReportViewProps) {
  const [detailedRatings, setDetailedRatings] = useState<UiDetailedRating[]>([]);
  const [note, setNote] = useState("");
  const [recommendation, setRecommendation] = useState<Recommendation>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const addRating = () => {
    setDetailedRatings([
      ...detailedRatings,
      {
        id: Math.random().toString(36).substr(2, 9),
        type: "",
        rating: 5,
        comment: "",
        weight: 0.0,
      },
    ]);
  };

  const updateRating = (id: string, field: keyof UiDetailedRating, value: string | number) => {
    setDetailedRatings(detailedRatings.map((r) => (r.id === id ? { ...r, [field]: value } : r)));
  };

  const deleteRating = (id: string) => {
    setDetailedRatings(detailedRatings.filter((r) => r.id !== id));
  };

  const handleAutoBalanceWeights = () => {
    setDetailedRatings(autoBalanceWeights(detailedRatings));
  };

  /**
   * Runs the E2–E5 client-side validation rules against the current form state.
   *
   * @returns The list of human-readable violation messages (empty when valid).
   */
  const validateForm = (): string[] => {
    const errors: string[] = [];

    // E2: At least one rating
    if (detailedRatings.length === 0) {
      errors.push("E2: Add at least one detailed rating");
    }

    // E3: Weights must sum to 1.0
    const weightSum = calculateWeightSum(detailedRatings);
    if (Math.abs(weightSum - 1.0) > 0.001) {
      errors.push(`E3: Weights must sum to exactly 1.0. Current: ${weightSum.toFixed(3)}`);
    }

    // E4: Rating and weight ranges + comments required
    detailedRatings.forEach((r, idx) => {
      if (r.rating < 1 || r.rating > 10) {
        errors.push(`E4: Rating #${idx + 1} must be between 1-10`);
      }
      if (r.weight < 0 || r.weight > 1) {
        errors.push(`E4: Weight #${idx + 1} must be between 0.0-1.0`);
      }
      // Check if comment is required and empty
      if (!r.comment || r.comment.trim() === "") {
        errors.push(`E4: Comment for rating #${idx + 1} is required`);
      }
    });

    // E5: Note and Recommendation required
    if (!note || note.trim() === "") {
      errors.push("E5: Note is required");
    }

    if (!recommendation) {
      errors.push("E5: Draft recommendation is required");
    }

    return errors;
  };

  const handleSubmit = () => {
    const errors = validateForm();
    setValidationErrors(errors);
    if (errors.length > 0) return;

    onSubmit({ detailedRatings, note, recommendation });
  };

  const weightSum = calculateWeightSum(detailedRatings);
  const finalRating = calculateFinalRating(detailedRatings);

  return (
    <div className="flex gap-6 p-8">
      {/* Column 1 - Form */}
      <div className="flex-1 max-w-[720px] space-y-6">
        {/* Header */}
        <div className="flex items-start justify-between sticky top-0 bg-[#0B0B0F] pb-4 z-10">
          <div>
            <h2 className="text-3xl font-bold mb-1">New Scouting Report</h2>
            <p className="text-sm text-[#6B6B75]">
              {player.firstName} {player.lastName} - {matches.length} match{matches.length !== 1 ? 'es' : ''} in scope
            </p>
          </div>
        </div>

        {/* Validation Errors */}
        {validationErrors.length > 0 && (
          <div className="bg-[#E5484D]/10 border border-[#E5484D] rounded-lg p-4">
            <div className="flex items-start gap-2">
              <AlertCircle className="w-5 h-5 text-[#E5484D] flex-shrink-0 mt-0.5" />
              <div className="flex-1">
                <div className="font-semibold text-[#E5484D] mb-2">Please fix the following errors:</div>
                <ul className="space-y-1 text-sm text-[#E5484D]">
                  {validationErrors.map((error: string, idx: number) => (
                    <li key={idx}>• {error}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        {/* Card 1 - Match Scope */}
        <div className="bg-[#14141A] rounded-2xl p-6 border border-[#26262F]">
          <h3 className="text-lg font-semibold mb-4">Match Scope</h3>
          <div className="space-y-2 mb-4">
            {matches.map((match: UiMatch) => (
              <div key={match.id} className="text-sm flex items-center justify-between py-2 border-b border-[#26262F] last:border-0">
                <span className="text-[#A1A1AA]">
                  {match.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                </span>
                <span className="font-medium" style={{ fontFeatureSettings: '"tnum"' }}>
                  vs {match.host.name === player.club.name ? match.guest.name : match.host.name} • {match.host_score}-{match.guest_score}
                </span>
              </div>
            ))}
          </div>
          {/* Averages panel: backend aggregate (player.kpi) in the default flow,
              hidden in the A1 subset flow because the aggregate would not match
              the selected subset and the frontend must not recompute. */}
          {!matchSubsetSelected && (
            <div className="flex gap-3 flex-wrap">
              <StatChip label="MIN" value={player.kpi.mpg.toFixed(1)} />
              <StatChip label="PTS" value={player.kpi.ppg.toFixed(1)} />
              <StatChip label="REB" value={player.kpi.rpg.toFixed(1)} />
              <StatChip label="AST" value={player.kpi.apg.toFixed(1)} />
              <StatChip label="STL" value={player.kpi.spg.toFixed(1)} />
              <StatChip label="BLK" value={player.kpi.bpg.toFixed(1)} />
            </div>
          )}
        </div>

        {/* Card 2 - Detailed Ratings */}
        <div className="bg-[#14141A] rounded-2xl p-6 border border-[#26262F]">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold">Detailed Ratings</h3>
            <div className="text-right">
              <div className={`text-sm font-medium ${
                Math.abs(weightSum - 1.0) < 0.001 ? 'text-[#2EBD85]' : weightSum > 1.0 ? 'text-[#E5484D]' : 'text-[#F4B740]'
              }`} style={{ fontFeatureSettings: '"tnum"' }}>
                Weights: {weightSum.toFixed(3)} / 1.000
              </div>
              <div className="w-32 h-2 bg-[#26262F] rounded-full overflow-hidden mt-1">
                <div
                  className={`h-full transition-all ${
                    Math.abs(weightSum - 1.0) < 0.001 ? 'bg-[#2EBD85]' : weightSum > 1.0 ? 'bg-[#E5484D]' : 'bg-[#F4B740]'
                  }`}
                  style={{ width: `${Math.min(weightSum * 100, 100)}%` }}
                />
              </div>
            </div>
          </div>

          {detailedRatings.length === 0 ? (
            <button
              onClick={addRating}
              className="w-full border-2 border-dashed border-[#3A3A45] hover:border-[#FF6A1A] rounded-lg p-8 text-[#A1A1AA] hover:text-[#FF6A1A] transition-colors flex items-center justify-center gap-2"
            >
              <Plus className="w-5 h-5" />
              Add first rating
            </button>
          ) : (
            <div className="space-y-4">
              {detailedRatings.map((rating: UiDetailedRating) => (
                <DetailedRatingRow
                  key={rating.id}
                  rating={rating}
                  onUpdate={updateRating}
                  onDelete={deleteRating}
                />
              ))}
              <div className="flex items-center justify-between pt-2">
                <button
                  onClick={addRating}
                  className="text-sm text-[#FF6A1A] hover:text-[#FF8033] font-medium flex items-center gap-1 transition-colors"
                >
                  <Plus className="w-4 h-4" />
                  Add rating
                </button>
                <button
                  onClick={handleAutoBalanceWeights}
                  className="text-sm text-[#FF6A1A] hover:text-[#FF8033] font-medium transition-colors"
                >
                  Auto-balance weights
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Card 3 - Note */}
        <div className="bg-[#14141A] rounded-2xl p-6 border border-[#26262F]">
          <h3 className="text-lg font-semibold mb-4">Note <span className="text-[#E5484D]">*</span></h3>
          <textarea
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Required: Describe what you observed: strengths, weaknesses, comparisons, development trajectory..."
            className="w-full bg-[#1C1C24] border border-[#26262F] rounded-lg p-4 text-sm text-[#F5F5F7] placeholder:text-[#6B6B75] focus:outline-none focus:ring-2 focus:ring-[#FFA366]/30 focus:border-[#FF6A1A] resize-none"
            rows={6}
            maxLength={2000}
          />
          <div className="text-xs text-[#6B6B75] text-right mt-2">
            {note.length} / 2000
          </div>
        </div>

        {/* Card 4 - Recommendation */}
        <div className="bg-[#14141A] rounded-2xl p-6 border border-[#26262F]">
          <h3 className="text-lg font-semibold mb-4">Draft Recommendation <span className="text-[#E5484D]">*</span></h3>
          <div className="grid grid-cols-2 gap-3">
            <RecommendationCard
              type="strong_buy"
              label="STRONG BUY"
              description="High-priority target. Aggressive pursuit."
              selected={recommendation === "strong_buy"}
              onClick={() => setRecommendation("strong_buy")}
            />
            <RecommendationCard
              type="buy"
              label="BUY"
              description="Clear interest. Continue tracking."
              selected={recommendation === "buy"}
              onClick={() => setRecommendation("buy")}
            />
            <RecommendationCard
              type="neutral"
              label="NEUTRAL"
              description="Wait-and-see."
              selected={recommendation === "neutral"}
              onClick={() => setRecommendation("neutral")}
            />
            <RecommendationCard
              type="pass"
              label="PASS"
              description="Not a fit at this stage."
              selected={recommendation === "pass"}
              onClick={() => setRecommendation("pass")}
            />
          </div>
        </div>
      </div>

      {/* Column 2 - Sticky Summary */}
      <div className="w-[320px] flex-shrink-0">
        <div className="sticky top-8 bg-[#14141A] rounded-2xl p-6 border border-[#26262F] space-y-6">
          <div>
            <div className="text-sm text-[#6B6B75] mb-2">Live Preview</div>
            <div className="text-5xl font-bold mb-2" style={{ fontFeatureSettings: '"tnum"' }}>
              {finalRating.toFixed(2)}
            </div>
            <div className="text-sm text-[#6B6B75]">Final Rating / 10</div>
          </div>

          {detailedRatings.length > 0 && (
            <div className="space-y-2">
              <div className="text-sm font-medium text-[#A1A1AA] mb-3">Rating Breakdown</div>
              {detailedRatings.map((rating: UiDetailedRating) => (
                <div key={rating.id} className="flex items-center justify-between text-sm">
                  <span className="text-[#6B6B75] truncate flex-1">{rating.type || 'Unnamed'}</span>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{rating.rating}</span>
                    <span className="text-[#6B6B75]">×</span>
                    <span className="text-[#6B6B75]">{rating.weight.toFixed(2)}</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {recommendation && (
            <div>
              <div className="text-sm font-medium text-[#A1A1AA] mb-2">Recommendation</div>
              <div className={`px-3 py-2 rounded-lg text-sm font-medium text-center ${
                recommendation === 'strong_buy' ? 'bg-[#2EBD85]/20 text-[#2EBD85]' :
                recommendation === 'buy' ? 'bg-[#FF6A1A]/20 text-[#FF6A1A]' :
                recommendation === 'neutral' ? 'bg-[#6B6B75]/20 text-[#A1A1AA]' :
                'bg-[#E5484D]/20 text-[#E5484D]'
              }`}>
                {recommendation.toUpperCase().replace('_', ' ')}
              </div>
            </div>
          )}

          <div>
            <div className="text-sm font-medium text-[#A1A1AA] mb-2">Status</div>
            <div className={`px-3 py-2 rounded-lg text-sm font-medium text-center ${
              Math.abs(weightSum - 1.0) < 0.001 ? 'bg-[#2EBD85]/20 text-[#2EBD85]' : 
              weightSum > 1.0 ? 'bg-[#E5484D]/20 text-[#E5484D]' : 
              'bg-[#F4B740]/20 text-[#F4B740]'
            }`}>
              {Math.abs(weightSum - 1.0) < 0.001 ? '✓ Valid' : weightSum > 1.0 ? 'Over 1.0' : 'Under 1.0'}
            </div>
          </div>

          <div className="flex flex-col items-center gap-3">
            <button
              onClick={handleSubmit}
              className="w-full py-3 rounded-lg font-medium transition-all text-center bg-[#FF6A1A] hover:bg-[#FF8033] text-white"
            >
              Submit Report
            </button>

            <button
              onClick={onCancel}
              className="px-4 py-2 bg-transparent border-2 border-[#E5484D]/30 hover:bg-[#E5484D]/10 hover:border-[#E5484D] text-[#E5484D] rounded-lg font-medium transition-colors"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
