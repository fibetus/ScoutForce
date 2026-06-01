import { useState } from "react";
import { Trash2 } from "lucide-react";
import type { UiDetailedRating } from "../types/domain";

/**
 * Suggested rating categories offered through the `<datalist>` autocomplete
 * for the "Type" field. Ported 1:1 from the prototype.
 */
const RATING_SUGGESTIONS = ["offense", "defense", "athleticism", "basketball IQ", "character", "scoring", "playmaking", "rebounding", "shot creation", "motor"];

/**
 * Props for {@link DetailedRatingRow}.
 */
export interface DetailedRatingRowProps {
  /** The detailed rating row state being edited. */
  rating: UiDetailedRating;
  /**
   * Invoked when a field of the row changes.
   *
   * @param id - Identifier of the row being edited.
   * @param field - The field of the rating being updated.
   * @param value - The new value (string for text fields, number for rating/weight).
   */
  onUpdate: (id: string, field: keyof UiDetailedRating, value: string | number) => void;
  /**
   * Invoked when the user removes this row.
   *
   * @param id - Identifier of the row to delete.
   */
  onDelete: (id: string) => void;
}

/**
 * Editable row for a single detailed rating inside the create-report form.
 *
 * Renders inputs for the rating type (with autocomplete suggestions), the
 * numeric rating (1-10) and weight (0.0-1.0), and a required comment, plus a
 * delete button. All edits are propagated upward through `onUpdate`/`onDelete`;
 * the only local state is `showComment`, preserved from the prototype.
 */
export function DetailedRatingRow({ rating, onUpdate, onDelete }: DetailedRatingRowProps) {
  const [showComment, setShowComment] = useState(true); // Always show comment since it's required

  return (
    <div className="bg-[#1C1C24] rounded-lg p-4 space-y-3">
      <div className="flex items-start gap-3">
        <div className="flex-1 space-y-3">
          <div>
            <label className="text-xs text-[#6B6B75] mb-1 block">Type</label>
            <input
              type="text"
              value={rating.type}
              onChange={(e) => onUpdate(rating.id, 'type', e.target.value)}
              list="rating-suggestions"
              placeholder="e.g., offense, defense..."
              className="w-full bg-[#0B0B0F] border border-[#26262F] rounded-md px-3 py-2 text-sm text-[#F5F5F7] placeholder:text-[#6B6B75] focus:outline-none focus:ring-2 focus:ring-[#FFA366]/30 focus:border-[#FF6A1A]"
            />
            <datalist id="rating-suggestions">
              {RATING_SUGGESTIONS.map(s => <option key={s} value={s} />)}
            </datalist>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-[#6B6B75] mb-1 block">Rating (1-10)</label>
              <input
                type="number"
                min="1"
                max="10"
                value={rating.rating}
                onChange={(e) => onUpdate(rating.id, 'rating', parseInt(e.target.value) || 1)}
                className="w-full bg-[#0B0B0F] border border-[#26262F] rounded-md px-3 py-2 text-sm text-[#F5F5F7] focus:outline-none focus:ring-2 focus:ring-[#FFA366]/30 focus:border-[#FF6A1A]"
              />
            </div>
            <div>
              <label className="text-xs text-[#6B6B75] mb-1 block">Weight (0.0-1.0)</label>
              <input
                type="number"
                min="0"
                max="1"
                step="0.05"
                value={rating.weight}
                onChange={(e) => onUpdate(rating.id, 'weight', parseFloat(e.target.value) || 0)}
                className="w-full bg-[#0B0B0F] border border-[#26262F] rounded-md px-3 py-2 text-sm text-[#F5F5F7] focus:outline-none focus:ring-2 focus:ring-[#FFA366]/30 focus:border-[#FF6A1A]"
              />
            </div>
          </div>

          <div>
            <label className="text-xs text-[#6B6B75] mb-1 block">Comment <span className="text-[#E5484D]">*</span></label>
            <textarea
              value={rating.comment}
              onChange={(e) => onUpdate(rating.id, 'comment', e.target.value)}
              placeholder="Required: Add your observations..."
              className="w-full bg-[#0B0B0F] border border-[#26262F] rounded-md px-3 py-2 text-sm text-[#F5F5F7] placeholder:text-[#6B6B75] focus:outline-none focus:ring-2 focus:ring-[#FFA366]/30 focus:border-[#FF6A1A] resize-none"
              rows={2}
              maxLength={500}
            />
          </div>
        </div>

        <button
          onClick={() => onDelete(rating.id)}
          className="p-2 text-[#6B6B75] hover:text-[#E5484D] transition-colors flex-shrink-0"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
