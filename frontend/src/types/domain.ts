/**
 * UI domain types consumed by the ported prototype components.
 *
 * These types describe the shapes the React components expect after the API
 * adapters have translated backend DTOs. They intentionally mirror the field
 * names used in the original prototype (e.g. `player_status`, `minutes_played`,
 * `host_score`) so the ported JSX can stay unchanged. No domain calculations
 * live here; the adapter layer is responsible for producing these shapes.
 */

/**
 * Identifies the currently rendered view in the single-page application.
 *
 * The values match the navigation flow of the prototype:
 * `players` -> `player-detail` -> `player-matches` -> `create-report`
 * -> `report-success`.
 */
export type View =
  | "players"
  | "player-detail"
  | "player-matches"
  | "create-report"
  | "report-success";

/**
 * Scouting recommendation expressed in the UI (lowercase) representation.
 *
 * `null` represents the state where the scout has not yet picked a
 * recommendation in the create-report form.
 */
export type Recommendation = "strong_buy" | "buy" | "neutral" | "pass" | null;

/**
 * Player key performance indicators (per-game averages) provided by the
 * backend and shown on the KPI tiles and the averages panel.
 *
 * All values are plain numbers; presentational rounding (e.g. one decimal
 * place) is applied by the components at render time.
 */
export interface UiKpi {
  /** Points per game (maps to backend `avgPoints`). */
  ppg: number;
  /** Rebounds per game (maps to backend `avgRebounds`). */
  rpg: number;
  /** Assists per game (maps to backend `avgAssists`). */
  apg: number;
  /** Minutes per game (maps to backend `avgMinutes`). */
  mpg: number;
  /** Steals per game (maps to backend `avgSteals`). */
  spg: number;
  /** Blocks per game (maps to backend `avgBlocks`). */
  bpg: number;
}

/**
 * A player as consumed by the UI views (list item and detail view).
 *
 * Field names follow the prototype so the ported components require no
 * structural changes. Physical measurements are expressed in metric units
 * (centimetres and kilograms) per the accepted deviation from the prototype.
 */
export interface UiPlayer {
  /** Backend player identifier. */
  id: number;
  /** Player given name. */
  firstName: string;
  /** Player family name. */
  lastName: string;
  /** Date of birth as a JavaScript `Date` instance. */
  dateOfBirth: Date;
  /** Position abbreviation, e.g. "C", "PG" (mapped from `PositionType`). */
  position: string;
  /**
   * Player status key in lowercase enum form, e.g. "observed".
   * Used as a key into the `statusColors` map in `PlayerListItem`.
   */
  player_status: string;
  /** Weight in kilograms. */
  weight: number;
  /** Height in centimetres. */
  height: number;
  /** Arm span (wingspan) in centimetres. */
  wingspan: number;
  /** Average rating in the range 0..10 (0 when the player has no reports). */
  averageRating: number;
  /** Club information shown alongside the player. */
  club: { name: string; city: string; league: string };
  /** Per-game averages from the backend, used by tiles and the averages panel. */
  kpi: UiKpi;
}

/**
 * Box-score statistics for a single player in a single match.
 *
 * Field names follow the prototype (snake_case) so the `MatchCard` component
 * can render them without changes.
 */
export interface UiMatchStats {
  /** Minutes played (maps to backend `minutesPlayed`). */
  minutes_played: number;
  /** Points scored. */
  points: number;
  /** Total rebounds. */
  rebounds: number;
  /** Assists. */
  assists: number;
  /** Steals. */
  steals: number;
  /** Blocks. */
  blocks: number;
}

/**
 * A match together with the selected player's box-score, as consumed by the
 * matches view and `MatchCard`.
 */
export interface UiMatch {
  /** Backend match identifier. */
  id: number;
  /** Identifier of the player whose stats are attached, injected from the request context. */
  playerId: number;
  /** Match date as a JavaScript `Date` instance. */
  date: Date;
  /** Venue / location of the match. */
  place: string;
  /** Home club. */
  host: { name: string; city: string };
  /** Away club. */
  guest: { name: string; city: string };
  /** Final score of the home club. */
  host_score: number;
  /** Final score of the away club. */
  guest_score: number;
  /** The selected player's box-score for this match. */
  stats: UiMatchStats;
}

/**
 * A single detailed rating row in the create-report form.
 *
 * This represents transient form state (Report_Form_Data) edited locally in
 * the UI before being submitted to the backend.
 */
export interface UiDetailedRating {
  /** Client-generated row identifier. */
  id: string;
  /** Free-text category/type of the rating. */
  type: string;
  /** Rating value, expected within the range [1, 10]. */
  rating: number;
  /** Free-text comment for this rating. */
  comment: string;
  /** Weight of this rating, expected within the range [0.0, 1.0]. */
  weight: number;
}

/**
 * Result of saving a scouting report, shown on the success screen.
 *
 * The `finalRating` is the authoritative value returned by the backend and
 * replaces any client-side preview.
 */
export interface UiReportResult {
  /** Final rating computed by the backend (source of truth). */
  finalRating: number;
  /** Recommendation selected for the saved report. */
  recommendation: Recommendation;
}
