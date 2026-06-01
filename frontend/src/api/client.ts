/**
 * Typed HTTP client for the Spring Boot backend.
 *
 * `ApiClient` is a thin wrapper around `fetch` that owns transport concerns only:
 * JSON content negotiation, request timeouts via `AbortController`, decoding of
 * backend `ApiErrorDto` bodies into thrown {@link ApiError} instances, and the
 * `204 No Content` convention. It delegates all DTO-to-UI translation to the
 * adapter layer (see `./adapters`) and performs no domain calculations of its own.
 *
 * Endpoints are all scoped under `${API_BASE_URL}/api/scouts/{scoutId}`.
 */

import { API_BASE_URL } from "./config";
import {
  ApiError,
  adaptMatch,
  adaptPlayer,
  toRecommendationUi,
} from "./adapters";
import type {
  ApiErrorDto,
  CreateReportFromMatchesPayload,
  CreateReportPayload,
  MatchWithStatsDto,
  PlayerDto,
} from "./dto";
import type { UiMatch, UiPlayer, UiReportResult } from "../types/domain";

/**
 * Request timeout in milliseconds.
 *
 * A request that exceeds this threshold is aborted and surfaced as an
 * {@link ApiError} with status `0` (treated by the UI as a connection failure).
 */
const REQUEST_TIMEOUT_MS = 30_000;

/**
 * Subset of the backend `ScoutingReport` JSON consumed when creating a report.
 *
 * The backend returns the full persisted report; only the authoritative
 * `finalRating` (derived server-side) and the `recommendation` enum name are
 * needed to build a {@link UiReportResult}. Other fields are ignored.
 */
interface ScoutingReportResponseDto {
  /** Final rating computed by the backend (source of truth). */
  finalRating: number;
  /** Recommendation as the backend `RecommendationType` enum name (e.g. `"STRONG_BUY"`). */
  recommendation: string;
}

/**
 * Typed client for the backend REST API.
 *
 * Construct with an explicit base URL for testing or alternative environments;
 * by default it targets {@link API_BASE_URL}. A shared {@link apiClient} instance
 * is exported for convenient use from `App.tsx`.
 */
export class ApiClient {
  /**
   * @param baseUrl - Base URL of the backend (defaults to {@link API_BASE_URL}).
   */
  constructor(private readonly baseUrl: string = API_BASE_URL) {}

  /**
   * Fetches the players observed by the given scout.
   *
   * Calls `GET /api/scouts/{scoutId}/players` and maps each `PlayerDto` to a
   * {@link UiPlayer} via the adapter layer.
   *
   * @param scoutId - Identifier of the scout whose observed players are requested.
   * @returns The observed players in UI shape.
   * @throws {ApiError} On a non-2xx response, network failure, or timeout.
   */
  async getObservablePlayers(scoutId: number): Promise<UiPlayer[]> {
    const dtos = await this.request<PlayerDto[]>(
      `/api/scouts/${scoutId}/players`,
    );
    return (dtos ?? []).map(adaptPlayer);
  }

  /**
   * Fetches the matches of a player that were observed by the given scout.
   *
   * Calls `GET /api/scouts/{scoutId}/players/{playerId}/matches`. A
   * `204 No Content` response (the scout observed none of the player's matches)
   * is treated as an empty list. Each `MatchWithStatsDto` is mapped to a
   * {@link UiMatch} via the adapter, injecting `playerId` from the request context.
   *
   * @param scoutId - Identifier of the observing scout.
   * @param playerId - Identifier of the player whose matches are requested.
   * @returns The observed matches in UI shape, or an empty array on `204`.
   * @throws {ApiError} On a non-2xx response, network failure, or timeout.
   */
  async getPlayerMatches(
    scoutId: number,
    playerId: number,
  ): Promise<UiMatch[]> {
    const dtos = await this.request<MatchWithStatsDto[]>(
      `/api/scouts/${scoutId}/players/${playerId}/matches`,
    );
    if (dtos === null) {
      return [];
    }
    return dtos.map((dto) => adaptMatch(dto, playerId));
  }

  /**
   * Creates a scouting report over all of the player's observed matches (default flow).
   *
   * Calls `POST /api/scouts/{scoutId}/players/{playerId}/reports`. The returned
   * `finalRating` is the backend's authoritative value and replaces any
   * client-side preview.
   *
   * @param scoutId - Identifier of the authoring scout.
   * @param playerId - Identifier of the player being reported on.
   * @param payload - The report body (note, recommendation, detailed ratings).
   * @returns The saved report's final rating and recommendation in UI shape.
   * @throws {ApiError} On a non-2xx response, network failure, or timeout.
   */
  async createReport(
    scoutId: number,
    playerId: number,
    payload: CreateReportPayload,
  ): Promise<UiReportResult> {
    const report = await this.request<ScoutingReportResponseDto>(
      `/api/scouts/${scoutId}/players/${playerId}/reports`,
      { method: "POST", body: JSON.stringify(payload) },
    );
    return this.toReportResult(report);
  }

  /**
   * Creates a scouting report scoped to a selected subset of matches (flow A1).
   *
   * Calls `POST /api/scouts/{scoutId}/players/{playerId}/reports/from-matches`
   * with the selected `matchIds`. The returned `finalRating` is the backend's
   * authoritative value.
   *
   * @param scoutId - Identifier of the authoring scout.
   * @param playerId - Identifier of the player being reported on.
   * @param payload - The report body including the selected `matchIds`.
   * @returns The saved report's final rating and recommendation in UI shape.
   * @throws {ApiError} On a non-2xx response, network failure, or timeout.
   */
  async createReportFromMatches(
    scoutId: number,
    playerId: number,
    payload: CreateReportFromMatchesPayload,
  ): Promise<UiReportResult> {
    const report = await this.request<ScoutingReportResponseDto>(
      `/api/scouts/${scoutId}/players/${playerId}/reports/from-matches`,
      { method: "POST", body: JSON.stringify(payload) },
    );
    return this.toReportResult(report);
  }

  // ---------------------------------------------------------------------------
  // Internals
  // ---------------------------------------------------------------------------

  /**
   * Executes a JSON request against the backend with a hard timeout.
   *
   * Sets `Content-Type: application/json`, aborts after {@link REQUEST_TIMEOUT_MS}
   * via `AbortController`, and applies the transport error policy:
   * - non-2xx response -> throws {@link ApiError} built from the `ApiErrorDto` body
   *   (or the status text / a generic message when the body is not parseable);
   * - network failure or timeout -> throws `ApiError(0, ...)`;
   * - `204 No Content` -> resolves to `null`;
   * - otherwise resolves to the parsed JSON body typed as `T`.
   *
   * @typeParam T - Expected shape of the successful JSON response body.
   * @param path - Request path appended to the client's base URL.
   * @param init - Optional `fetch` init overrides (method, body, headers).
   * @returns The parsed response body, or `null` for a `204` response.
   * @throws {ApiError} On a non-2xx response, network failure, or timeout.
   */
  private async request<T>(
    path: string,
    init?: RequestInit,
  ): Promise<T | null> {
    const controller = new AbortController();
    const timeoutId = setTimeout(
      () => controller.abort(),
      REQUEST_TIMEOUT_MS,
    );

    try {
      let response: Response;
      try {
        response = await fetch(`${this.baseUrl}${path}`, {
          ...init,
          headers: {
            "Content-Type": "application/json",
            ...(init?.headers ?? {}),
          },
          signal: controller.signal,
        });
      } catch {
        // fetch rejects on network failure or when the request is aborted.
        const message = controller.signal.aborted
          ? `Request timed out after ${REQUEST_TIMEOUT_MS / 1000}s.`
          : "Network request failed.";
        throw new ApiError(0, message);
      }

      if (!response.ok) {
        throw await this.toApiError(response);
      }

      if (response.status === 204) {
        return null;
      }

      return (await response.json()) as T;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Builds an {@link ApiError} from a non-2xx response.
   *
   * Attempts to parse the body as {@link ApiErrorDto} and use its `message`;
   * falls back to the response status text or a generic message when the body is
   * absent or not valid JSON.
   *
   * @param response - The non-2xx response.
   * @returns The error to throw, carrying the HTTP status and a message.
   */
  private async toApiError(response: Response): Promise<ApiError> {
    try {
      const body = (await response.json()) as Partial<ApiErrorDto>;
      if (typeof body?.message === "string" && body.message.length > 0) {
        return new ApiError(response.status, body.message);
      }
    } catch {
      // Body was empty or not valid JSON; fall through to a generic message.
    }
    const fallback =
      response.statusText.length > 0
        ? response.statusText
        : `Request failed with status ${response.status}.`;
    return new ApiError(response.status, fallback);
  }

  /**
   * Converts a backend report response into a {@link UiReportResult}.
   *
   * Uses the backend `finalRating` as the source of truth and maps the
   * recommendation enum name to its UI representation.
   *
   * @param report - The parsed report response (never `null` for a `200`).
   * @returns The UI-shaped report result.
   * @throws {ApiError} If the response body is unexpectedly empty.
   */
  private toReportResult(
    report: ScoutingReportResponseDto | null,
  ): UiReportResult {
    if (report === null) {
      throw new ApiError(0, "Empty response while creating a report.");
    }
    return {
      finalRating: Number(report.finalRating ?? 0),
      recommendation: toRecommendationUi(report.recommendation),
    };
  }
}

/**
 * Shared {@link ApiClient} instance targeting {@link API_BASE_URL}.
 *
 * Convenient for direct use from `App.tsx`; construct a dedicated instance when a
 * different base URL is required.
 */
export const apiClient = new ApiClient();
