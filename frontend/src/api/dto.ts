/**
 * Backend data transfer object (DTO) types.
 *
 * These interfaces mirror the Spring Boot web-layer Java records exactly, so the
 * JSON payloads exchanged with the backend are strongly typed on the frontend.
 * Date fields are ISO date strings (`LocalDate` serialized as `"YYYY-MM-DD"`) and
 * numeric fields map Java `Double`/`BigDecimal`/`Integer` values to TypeScript
 * `number`. These types describe the raw wire format only; translation into the
 * UI domain shapes happens in the adapter layer.
 */

/**
 * Minimal club information consumed by the UI.
 *
 * Mirrors the backend `ClubDto` record. Used both for a player's club and for the
 * host/guest clubs of a match.
 */
export interface ClubDto {
  /** The club name. */
  name: string;
  /** The city the club is based in. */
  city: string;
  /** The league the club competes in. */
  league: string;
}

/**
 * A player's aggregated box-score averages (KPI), computed server-side.
 *
 * Mirrors the backend `PlayerKpiDto` record. Averages are calculated over the
 * matches observed by the scout in which the player appeared. The frontend only
 * displays these values and performs no domain calculations of its own.
 */
export interface PlayerKpiDto {
  /** Average minutes played per match. */
  avgMinutes: number;
  /** Average points scored per match. */
  avgPoints: number;
  /** Average rebounds per match. */
  avgRebounds: number;
  /** Average assists per match. */
  avgAssists: number;
  /** Average steals per match. */
  avgSteals: number;
  /** Average blocks per match. */
  avgBlocks: number;
}

/**
 * A player together with the data required to render the player list and detail views.
 *
 * Mirrors the backend `PlayerDto` record. `position` and `playerStatus` are the raw
 * enum names; the adapter layer maps them to the UI representations. Physical
 * measurements use metric units (cm/kg) and are passed through without conversion.
 */
export interface PlayerDto {
  /** Unique player identifier. */
  id: number;
  /** Player's first name. */
  firstName: string;
  /** Player's last name. */
  lastName: string;
  /** Date of birth as an ISO date string (`"YYYY-MM-DD"`). */
  birthDate: string;
  /** Playing position as the backend `PositionType` enum name. */
  position:
    | "POINT_GUARD"
    | "SHOOTING_GUARD"
    | "SMALL_FORWARD"
    | "POWER_FORWARD"
    | "CENTER";
  /** Scouting status as the backend `PlayerStatus` enum name. */
  playerStatus:
    | "NEW"
    | "OBSERVED"
    | "MEDICAL_VERIFICATION"
    | "INVITED_TO_WORKOUT"
    | "INVITED_TO_BIG_BOARD"
    | "DELISTED";
  /** Weight in kilograms. */
  weight: number;
  /** Height in centimeters. */
  height: number;
  /** Wingspan in centimeters. */
  wingspan: number;
  /** Average final rating across the player's reports; `0` when there are none. */
  averageRating: number;
  /** The player's club. */
  club: ClubDto;
  /** Aggregated box-score averages over matches observed by the scout. */
  kpi: PlayerKpiDto;
}

/**
 * A single player's box-score for a single match.
 *
 * Mirrors the backend `MatchStatsDto` record. Exposes only the fields rendered by
 * the match card (MIN/PTS/REB/AST/STL/BLK); statistics that are not displayed are
 * deliberately omitted.
 */
export interface MatchStatsDto {
  /** Minutes played in the match. */
  minutesPlayed: number;
  /** Points scored in the match. */
  points: number;
  /** Rebounds in the match. */
  rebounds: number;
  /** Assists in the match. */
  assists: number;
  /** Steals in the match. */
  steals: number;
  /** Blocks in the match. */
  blocks: number;
}

/**
 * A match together with the box-score of the player referenced by the request URL.
 *
 * Mirrors the backend `MatchWithStatsDto` record. `date` is an ISO date string and
 * `stats` always belongs to the player whose matches were requested.
 */
export interface MatchWithStatsDto {
  /** Unique match identifier. */
  id: number;
  /** Match date as an ISO date string (`"YYYY-MM-DD"`). */
  date: string;
  /** Venue where the match was played. */
  place: string;
  /** Final score of the host club. */
  hostScore: number;
  /** Final score of the guest club. */
  guestScore: number;
  /** The host club. */
  host: ClubDto;
  /** The guest club. */
  guest: ClubDto;
  /** Box-score of the requested player in this match. */
  stats: MatchStatsDto;
}

/**
 * Error response body returned by the backend `GlobalExceptionHandler`.
 *
 * The `message` field carries the business-rule text that the UI surfaces to the
 * user. The adapter layer maps `status` + `message` onto the prototype's error UI.
 */
export interface ApiErrorDto {
  /** Server timestamp of the error as an ISO date-time string. */
  timestamp: string;
  /** HTTP status code. */
  status: number;
  /** HTTP status reason phrase (e.g. `"Unprocessable Entity"`). */
  error: string;
  /** Human-readable error message. */
  message: string;
}

/**
 * Request payload for a single detailed rating within a scouting report.
 *
 * Mirrors the backend request shape. `rating` is in the `[1, 10]` range and
 * `weight` in the `[0.0, 1.0]` range; weights across all ratings must sum to `1.0`.
 */
export interface DetailedRatingPayload {
  /** Category being rated (e.g. shooting, defense); must be non-empty. */
  type: string;
  /** Rating value in the `[1, 10]` range. */
  rating: number;
  /** Free-text justification; must be non-empty. */
  comment: string;
  /** Relative weight of this rating in the `[0.0, 1.0]` range. */
  weight: number;
}

/**
 * Request payload for creating a scouting report over all observed matches.
 *
 * Mirrors the backend request shape for `POST .../reports`. `recommendation` uses
 * the backend `RecommendationType` enum names.
 */
export interface CreateReportPayload {
  /** Free-text summary note; must be non-empty. */
  note: string;
  /** Overall recommendation as the backend `RecommendationType` enum name. */
  recommendation: "STRONG_BUY" | "BUY" | "NEUTRAL" | "PASS";
  /** Detailed ratings whose weights sum to `1.0`. */
  detailedRatings: DetailedRatingPayload[];
}

/**
 * Request payload for creating a scouting report scoped to a selected subset of matches.
 *
 * Mirrors the backend request shape for `POST .../reports/from-matches`. Extends
 * {@link CreateReportPayload} with the identifiers of the matches in scope.
 */
export interface CreateReportFromMatchesPayload extends CreateReportPayload {
  /** Identifiers of the matches the report is based on. */
  matchIds: number[];
}
