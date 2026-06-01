import { Plus, Circle, ThumbsUp, X as XIcon } from "lucide-react";
import type { LucideIcon } from "lucide-react";

/**
 * The non-null recommendation keys selectable in the create-report form.
 */
export type RecommendationCardType = "strong_buy" | "buy" | "neutral" | "pass";

/**
 * Props for {@link RecommendationCard}.
 */
export interface RecommendationCardProps {
  /** Recommendation key that selects the icon and accent colors. */
  type: RecommendationCardType;
  /** Bold label shown on the card (e.g. "STRONG BUY"). */
  label: string;
  /** Supporting description shown beneath the label. */
  description: string;
  /** Whether this card is the currently selected recommendation. */
  selected: boolean;
  /** Invoked when the card is activated. */
  onClick: () => void;
}

/**
 * Selectable recommendation tile used in the create-report form.
 *
 * Each card is keyed by a recommendation `type` that maps to a dedicated icon
 * and accent color. When `selected`, the card adopts its colored border and
 * tinted background; otherwise it uses the muted default style with a hover
 * affordance.
 */
export function RecommendationCard({ type, label, description, selected, onClick }: RecommendationCardProps) {
  const colors: Record<string, string> = {
    strong_buy: 'border-[#2EBD85] bg-[#2EBD85]/10',
    buy: 'border-[#FF6A1A] bg-[#FF6A1A]/10',
    neutral: 'border-[#6B6B75] bg-[#6B6B75]/10',
    pass: 'border-[#E5484D] bg-[#E5484D]/10'
  };

  const icons: Record<string, LucideIcon> = {
    strong_buy: ThumbsUp,
    buy: Plus,
    neutral: Circle,
    pass: XIcon
  };

  const Icon = icons[type];

  return (
    <button
      onClick={onClick}
      className={`p-4 rounded-lg border-2 transition-all text-left ${
        selected
          ? colors[type]
          : "border-[#26262F] bg-[#1C1C24] hover:border-[#3A3A45]"
      }`}
    >
      <Icon className="w-5 h-5 mb-2" />
      <div className="font-semibold text-sm mb-1">{label}</div>
      <div className="text-xs text-[#6B6B75]">{description}</div>
    </button>
  );
}
