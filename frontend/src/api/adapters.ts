/**
 * API adapter layer.
 *
 * This module is the thin boundary that translates backend DTO shapes (see
 * `./dto`) into the UI domain shapes consumed by the ported prototype components
 * (see `../types/domain`). It performs only field renaming/reshaping, enum
 * mapping, and request payload assembly. It deliberately performs NO domain
 * calculations: KPI, averages and box-score values arrive pre-computed from the
 * backend and are passed through unchanged (presentational rounding is applied by
 * components at render time).
 */

import type {
  ClubDto,
  MatchWithStatsDto,
  PlayerDto,
  PlayerKpiDto,
  DetailedRatingPayload,
  CreateReportPayload,
} from "./dto";
import type {
  Recommendation,
  UiDetailedRating,
  UiKpi,
  UiMatch,
  UiPlayer,
} from "../types/domain";

/** Backend `RecommendationType` enum names accepted in request payloads. */
type RecommendationEnum = CreateReportPayload["recommendation"];

// ---------------------------------------------------------------------------
// Enum mapping tables
// ---------------------------------------------------------------------------

/**
 * Maps a backend `PositionType` enum name to its UI abbreviation.
 *
 * A missing entry is treated as a hard mapping error rather than producing an
 * undefined UI value (see {@link adaptPosition}).
 */
const POSITION_TO_UI: Record<string, string> = {
  POINT_GUARD: "PG",
  SHOOTING_GUARD: "SG",
  SMALL_FORWARD: "SF",
  POWER_FORWARD: "PF",
  CENTER: "C",
};

/**
 * The set of valid UI `player_status` keys.
 *
 * Each value equals the lowercased backend `PlayerStatus` enum name and is also a
 * key in the `statusColors` map used by `PlayerListItem`.
 */
const PLAYER_STATUS_UI_KEYS: ReadonlySet<string> = new Set<string>([
  "new",
  "observed",
  "medical_verification",
  "invited_to_workout",
  "invited_to_big_board",
  "delisted",
]);

/**
 * Maps a UI (lowercase) recommendation to its backend `RecommendationType` enum name.
 *
 * A missing entry is treated as a hard mapping error (see {@link toRecommendationEnum}).
 */
const RECOMMENDATION_TO_ENUM: Record<string, RecommendationEnum> = {
  strong_buy: "STRONG_BUY",
  buy: "BUY",
  neutral: "NEUTRAL",
  pass: "PASS",
};

/**
 * Maps a backend `RecommendationType` enum name to its UI (lowercase) representation.
 *
 * A missing entry is treated as a hard mapping error (see {@link toRecommendationUi}).
 */
const RECOMMENDATION_TO_UI: Record<string, Recommendation> = {
  STRONG_BUY: "strong_buy",
  BUY: "buy",
  NEUTRAL: "neutral",
  PASS: "pass",
};

// ---------------------------------------------------------------------------
// Enum mapping functions
// ---------------------------------------------------------------------------

/**
 * Maps a backend `PositionType` enum name to its UI abbreviation (e.g. `CENTER` -> `C`).
 *
 * @param position - The backend `PositionType` enum name.
 * @returns The UI position abbreviation.
 * @throws {Error} If the value has no entry in the position mapping table.
 */
export function adaptPosition(position: string): string {
  const ui = POSITION_TO_UI[position];
  if (ui === undefined) {
    throw new Error(`Unknown PositionType value: "${position}"`);
  }
  return ui;
}

/**
 * Maps a backend `PlayerStatus` enum name to its UI status key.
 *
 * The key is the lowercased enum name (e.g. `OBSERVED` -> `observed`) and is
 * guaranteed to be one of the keys present in the `statusColors` map used by
 * `PlayerListItem`.
 *
 * @param status - The backend `PlayerStatus` enum name.
 * @returns The lowercase UI status key.
 * @throws {Error} If the lowercased value is not a recognised status key.
 */
export function adaptPlayerStatus(status: string): string {
  const key = status.toLowerCase();
  if (!PLAYER_STATUS_UI_KEYS.has(key)) {
    throw new Error(`Unknown PlayerStatus value: "${status}"`);
  }
  return key;
}

/**
 * Maps a backend `RecommendationType` enum name to its UI (lowercase) representation.
 *
 * @param value - The backend `RecommendationType` enum name (e.g. `STRONG_BUY`).
 * @returns The UI recommendation value (e.g. `strong_buy`).
 * @throws {Error} If the value has no entry in the recommendation mapping table.
 */
export function toRecommendationUi(value: string): Recommendation {
  const ui = RECOMMENDATION_TO_UI[value];
  if (ui === undefined) {
    throw new Error(`Unknown RecommendationType value: "${value}"`);
  }
  return ui;
}

/**
 * Maps a UI (lowercase) recommendation to its backend `RecommendationType` enum name.
 *
 * @param value - The UI recommendation value (e.g. `strong_buy`); must not be `null`.
 * @returns The backend `RecommendationType` enum name (e.g. `STRONG_BUY`).
 * @throws {Error} If the value is `null` or has no entry in the recommendation mapping table.
 */
export function toRecommendationEnum(value: Recommendation): RecommendationEnum {
  if (value === null) {
    throw new Error("Recommendation is required and cannot be null.");
  }
  const enumName = RECOMMENDATION_TO_ENUM[value];
  if (enumName === undefined) {
    throw new Error(`Unknown recommendation value: "${value}"`);
  }
  return enumName;
}

// ---------------------------------------------------------------------------
// Entity adapters
// ---------------------------------------------------------------------------

/**
 * Maps a backend `PlayerKpiDto` to the UI {@link UiKpi} shape (1:1, no calculations).
 *
 * Field mapping: `avgPoints` -> `ppg`, `avgRebounds` -> `rpg`, `avgAssists` -> `apg`,
 * `avgMinutes` -> `mpg`, `avgSteals` -> `spg`, `avgBlocks` -> `bpg`.
 *
 * @param kpi - The backend KPI DTO.
 * @returns The UI KPI shape.
 */
function adaptKpi(kpi: PlayerKpiDto): UiKpi {
  return {
    ppg: kpi.avgPoints,
    rpg: kpi.avgRebounds,
    apg: kpi.avgAssists,
    mpg: kpi.avgMinutes,
    spg: kpi.avgSteals,
    bpg: kpi.avgBlocks,
  };
}

/**
 * Maps a backend `PlayerDto` to the UI {@link UiPlayer} shape.
 *
 * Performs field renaming, enum mapping (position, status) and ISO-date parsing.
 * `averageRating` defaults to `0` when absent (`Number(averageRating ?? 0)`).
 * Physical measurements (height/wingspan in cm, weight in kg) are passed through
 * without conversion. No domain calculations are performed.
 *
 * @param dto - The backend player DTO.
 * @returns The UI player shape.
 * @throws {Error} If the player's position or status enum has no mapping entry.
 */
export function adaptPlayer(dto: PlayerDto): UiPlayer {
  return {
    id: dto.id,
    firstName: dto.firstName,
    lastName: dto.lastName,
    dateOfBirth: new Date(dto.birthDate),
    position: adaptPosition(dto.position),
    player_status: adaptPlayerStatus(dto.playerStatus),
    weight: dto.weight,
    height: dto.height,
    wingspan: dto.wingspan,
    averageRating: Number(dto.averageRating ?? 0),
    club: {
      name: dto.club.name,
      city: dto.club.city,
      league: dto.club.league,
    },
    kpi: adaptKpi(dto.kpi),
  };
}

/**
 * Maps a backend `MatchWithStatsDto` to the UI {@link UiMatch} shape.
 *
 * Performs field renaming (`hostScore` -> `host_score`, `minutesPlayed` ->
 * `minutes_played`, etc.), ISO-date parsing (`new Date(date)`) and injects the
 * `playerId` from the request context. The `stats` field carries the box-score of
 * exactly the requested player. No domain calculations are performed.
 *
 * @param dto - The backend match-with-stats DTO.
 * @param playerId - Identifier of the player whose box-score the match carries,
 *   taken from the request URL context.
 * @returns The UI match shape.
 */
export function adaptMatch(dto: MatchWithStatsDto, playerId: number): UiMatch {
  return {
    id: dto.id,
    playerId,
    date: new Date(dto.date),
    place: dto.place,
    host: adaptMatchClub(dto.host),
    guest: adaptMatchClub(dto.guest),
    host_score: dto.hostScore,
    guest_score: dto.guestScore,
    stats: {
      minutes_played: dto.stats.minutesPlayed,
      points: dto.stats.points,
      rebounds: dto.stats.rebounds,
      assists: dto.stats.assists,
      steals: dto.stats.steals,
      blocks: dto.stats.blocks,
    },
  };
}

/**
 * Reduces a backend `ClubDto` to the `{ name, city }` shape used by match cards.
 *
 * @param club - The backend club DTO.
 * @returns The minimal club shape consumed by `MatchCard`.
 */
function adaptMatchClub(club: ClubDto): { name: string; city: string } {
  return { name: club.name, city: club.city };
}

// ---------------------------------------------------------------------------
// Request payload builders
// ---------------------------------------------------------------------------

/**
 * Builds a backend `DetailedRatingPayload` from a UI detailed-rating row.
 *
 * Drops the client-only `id` field and forwards `type`, `rating`, `comment` and
 * `weight` unchanged. No domain calculations are performed.
 *
 * @param rating - The UI detailed-rating form row.
 * @returns The request payload for a single detailed rating.
 */
export function toDetailedRatingPayload(
  rating: UiDetailedRating,
): DetailedRatingPayload {
  return {
    type: rating.type,
    rating: rating.rating,
    comment: rating.comment,
    weight: rating.weight,
  };
}

// ---------------------------------------------------------------------------
// Error handling
// ---------------------------------------------------------------------------

/**
 * Error thrown by the API client for non-2xx backend responses and transport failures.
 *
 * Carries the HTTP `status` (or `0` for network errors / timeouts) alongside the
 * message. The UI maps instances of this error to user-facing banners via
 * {@link mapErrorToUi}.
 */
export class ApiError extends Error {
  /**
   * @param status - The HTTP status code, or `0` for a network error / timeout.
   * @param message - The error message (backend `message` when available).
   */
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

/**
 * Maps an error thrown during an API call to the list of user-facing messages
 * rendered by the prototype's `validationErrors` UI.
 *
 * Mapping rules:
 * - status `0` (no connection / timeout) -> the no-connection banner.
 * - status `500` -> the generic server-error banner.
 * - any other {@link ApiError} status (`422`/`400`/`404`) -> the backend `message`
 *   (business-rule text E1-E5), which is the source of truth.
 * - any non-`ApiError` value -> a generic unexpected-error banner.
 *
 * The user-facing strings are intentionally Polish per the design (UI copy).
 *
 * @param err - The thrown error (expected to be an {@link ApiError}).
 * @returns A list of user-facing messages.
 */
export function mapErrorToUi(err: unknown): string[] {
  if (err instanceof ApiError) {
    if (err.status === 0) {
      return ["Brak połączenia z serwerem. Spróbuj ponownie."];
    }
    if (err.status === 500) {
      return ["Błąd serwera. Spróbuj ponownie później."];
    }
    return [err.message];
  }
  return ["Wystąpił nieoczekiwany błąd."];
}
