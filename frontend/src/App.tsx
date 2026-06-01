import { useEffect, useState } from "react";

import { apiClient } from "./api/client";
import { CURRENT_SCOUT_ID } from "./api/config";
import {
  ApiError,
  mapErrorToUi,
  toDetailedRatingPayload,
  toRecommendationEnum,
} from "./api/adapters";
import type { CreateReportPayload } from "./api/dto";
import type {
  UiMatch,
  UiPlayer,
  UiReportResult,
  View,
} from "./types/domain";

import { PlayerListItem } from "./components/PlayerListItem";
import { PlayerDetailView } from "./components/PlayerDetailView";
import { PlayerMatchesView } from "./components/PlayerMatchesView";
import {
  CreateReportView,
  type CreateReportFormData,
} from "./components/CreateReportView";
import { ReportSuccessView } from "./components/ReportSuccessView";
import { EmptyState } from "./components/EmptyState";
import { Modal } from "./components/Modal";
import { Toast, type ToastType } from "./components/Toast";

/**
 * Lifetime, in milliseconds, of a transient {@link Toast} notification before it
 * is automatically dismissed. Mirrors the auto-dismiss timing of the prototype.
 */
const TOAST_TIMEOUT_MS = 5_000;

/**
 * A transient notification rendered by the {@link Toast} component.
 */
interface ToastState {
  /** Message text displayed inside the toast. */
  message: string;
  /** Severity that selects the toast color. */
  type: ToastType;
}

/**
 * Determines whether a caught error is the E1 business-rule violation
 * ("Chosen player has no matches you've observed.").
 *
 * E1 is reported by the backend as HTTP 422 carrying that message. It is
 * surfaced in the dedicated E1 banner of {@link PlayerDetailView} rather than as
 * a generic toast.
 *
 * @param err - The error thrown by an {@link ApiClient} call.
 * @returns `true` when the error is the E1 violation, otherwise `false`.
 */
function isE1Error(err: unknown): boolean {
  return (
    err instanceof ApiError &&
    err.status === 422 &&
    err.message.includes("no matches you've observed")
  );
}

/**
 * Root container of the ScoutForce SPA (the ported `ScoutForceNBA` component).
 *
 * This is the single stateful orchestrator of the application. It is a faithful
 * 1:1 port of the prototype's outer shell (`private/main.tsx`): a two-pane layout
 * with the player list pane (Pane A) on the left and a dynamic content pane
 * (Pane B) on the right, plus the cancel-confirmation {@link Modal} and the
 * {@link Toast} notification. The only behavioral change from the prototype is
 * the data source: instead of mock arrays, all data is loaded from the Spring
 * Boot backend through {@link apiClient}, with the adapter layer translating DTOs
 * into UI shapes.
 *
 * Responsibilities:
 * - **View state machine**: orchestrates transitions across
 *   `players` -> `player-detail` -> `player-matches` -> `create-report`
 *   -> `report-success`, preserving the selected player, the selected match
 *   subset and the active report flow. The initial view is `players`.
 * - **Data loading**: fetches the scout's observed players on mount via
 *   `getObservablePlayers(CURRENT_SCOUT_ID)`, and a player's observed matches on
 *   demand via `getPlayerMatches`, handling the `204 No Content` (no observed
 *   matches) case as an empty list.
 * - **Save endpoint selection**: uses `createReport` for the default flow (all
 *   observed matches) and `createReportFromMatches` for the A1 flow (an explicit
 *   subset of `matchIds`).
 * - **Error handling**: maps thrown {@link ApiError}s via {@link mapErrorToUi},
 *   surfacing the E1 condition in the {@link PlayerDetailView} banner and other
 *   failures (server error 500, no-connection/timeout, validation messages) via
 *   the players-pane message or a danger toast. The success screen always shows
 *   the backend-authoritative `finalRating`.
 */
export default function App() {
  // --- View + selection state -------------------------------------------------
  const [currentView, setCurrentView] = useState<View>("players");
  const [selectedPlayer, setSelectedPlayer] = useState<UiPlayer | null>(null);
  const [selectedMatches, setSelectedMatches] = useState<number[]>([]);
  /**
   * Whether the active report flow is the A1 subset flow (an explicit selection
   * of matches) as opposed to the default flow over all observed matches. Drives
   * both the averages-panel visibility in {@link CreateReportView} and the save
   * endpoint chosen on submit.
   */
  const [matchSubsetSelected, setMatchSubsetSelected] = useState(false);

  // --- Data state -------------------------------------------------------------
  const [players, setPlayers] = useState<UiPlayer[]>([]);
  const [playerMatches, setPlayerMatches] = useState<UiMatch[]>([]);
  const [reportResult, setReportResult] = useState<UiReportResult | null>(null);

  // --- UI / async state -------------------------------------------------------
  const [loadingPlayers, setLoadingPlayers] = useState(true);
  const [loadError, setLoadError] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [showE1Error, setShowE1Error] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [toast, setToast] = useState<ToastState | null>(null);

  /**
   * Loads the observed players for {@link CURRENT_SCOUT_ID} once on mount.
   * Failures are mapped to a user-facing message shown in the players pane.
   */
  useEffect(() => {
    let cancelled = false;
    const loadPlayers = async () => {
      setLoadingPlayers(true);
      setLoadError([]);
      try {
        const result = await apiClient.getObservablePlayers(CURRENT_SCOUT_ID);
        if (!cancelled) {
          setPlayers(result);
        }
      } catch (err) {
        if (!cancelled) {
          setLoadError(mapErrorToUi(err));
        }
      } finally {
        if (!cancelled) {
          setLoadingPlayers(false);
        }
      }
    };
    void loadPlayers();
    return () => {
      cancelled = true;
    };
  }, []);

  /**
   * Auto-dismisses the active toast after {@link TOAST_TIMEOUT_MS}.
   */
  useEffect(() => {
    if (toast === null) {
      return;
    }
    const timeoutId = setTimeout(() => setToast(null), TOAST_TIMEOUT_MS);
    return () => clearTimeout(timeoutId);
  }, [toast]);

  /**
   * Selects a player from the list and shows the player detail view, clearing
   * any stale E1 banner and match selection.
   *
   * @param player - The player chosen in the players pane.
   */
  const handlePlayerSelect = (player: UiPlayer) => {
    setSelectedPlayer(player);
    setShowE1Error(false);
    setSelectedMatches([]);
    setPlayerMatches([]);
    setCurrentView("player-detail");
  };

  /**
   * Loads the selected player's observed matches and shows the matches view.
   * A `204 No Content` (no observed matches) arrives as an empty list, which the
   * matches view renders as its empty state.
   */
  const handleViewMatches = async () => {
    if (selectedPlayer === null) {
      return;
    }
    setShowE1Error(false);
    try {
      const matches = await apiClient.getPlayerMatches(
        CURRENT_SCOUT_ID,
        selectedPlayer.id,
      );
      setPlayerMatches(matches);
      setSelectedMatches([]);
      setMatchSubsetSelected(false);
      setCurrentView("player-matches");
    } catch (err) {
      setToast({ message: mapErrorToUi(err).join(" "), type: "danger" });
    }
  };

  /**
   * Starts the default report flow from the player detail view.
   *
   * Loads the player's observed matches for scope display. When the player has
   * no observed matches (empty list / `204` / E1), the E1 banner is shown and the
   * view does not advance. Otherwise the create-report view opens in the default
   * flow (all observed matches, averages panel visible).
   */
  const handleCreateReport = async () => {
    if (selectedPlayer === null) {
      return;
    }
    setShowE1Error(false);
    try {
      const matches = await apiClient.getPlayerMatches(
        CURRENT_SCOUT_ID,
        selectedPlayer.id,
      );
      if (matches.length === 0) {
        // E1: the player has no matches observed by this scout.
        setShowE1Error(true);
        return;
      }
      setPlayerMatches(matches);
      setSelectedMatches(matches.map((m) => m.id));
      setMatchSubsetSelected(false);
      setCurrentView("create-report");
    } catch (err) {
      if (isE1Error(err)) {
        setShowE1Error(true);
        return;
      }
      setToast({ message: mapErrorToUi(err).join(" "), type: "danger" });
    }
  };

  /**
   * Proceeds from the matches view into the A1 report flow built from the
   * currently selected subset of matches. No-op when nothing is selected.
   */
  const handleCreateFromSelectedMatches = () => {
    if (selectedMatches.length === 0) {
      return;
    }
    setMatchSubsetSelected(true);
    setCurrentView("create-report");
  };

  /**
   * Toggles a match in or out of the selected subset.
   *
   * @param id - Identifier of the match toggled in the matches view.
   */
  const handleToggleMatch = (id: number) => {
    setSelectedMatches((prev) =>
      prev.includes(id) ? prev.filter((m) => m !== id) : [...prev, id],
    );
  };

  /**
   * Submits the assembled report form to the backend.
   *
   * Builds the request payload using {@link toDetailedRatingPayload} and
   * {@link toRecommendationEnum}, then calls `createReportFromMatches` (A1 flow,
   * including the selected `matchIds`) or `createReport` (default flow). On
   * success the success screen is shown with the backend-authoritative
   * `finalRating`. An E1 failure returns to the player detail view with the E1
   * banner; other failures surface a danger toast.
   *
   * @param formData - The validated form data emitted by {@link CreateReportView}.
   */
  const handleSubmitReport = async (formData: CreateReportFormData) => {
    if (selectedPlayer === null || submitting) {
      return;
    }
    setSubmitting(true);
    try {
      const basePayload: CreateReportPayload = {
        note: formData.note,
        recommendation: toRecommendationEnum(formData.recommendation),
        detailedRatings: formData.detailedRatings.map(toDetailedRatingPayload),
      };

      const result = matchSubsetSelected
        ? await apiClient.createReportFromMatches(
            CURRENT_SCOUT_ID,
            selectedPlayer.id,
            { ...basePayload, matchIds: selectedMatches },
          )
        : await apiClient.createReport(
            CURRENT_SCOUT_ID,
            selectedPlayer.id,
            basePayload,
          );

      setReportResult(result);
      setCurrentView("report-success");
      setToast({
        message: `Report saved. Final rating: ${result.finalRating.toFixed(2)}`,
        type: "success",
      });
    } catch (err) {
      if (isE1Error(err)) {
        setShowE1Error(true);
        setCurrentView("player-detail");
        return;
      }
      setToast({ message: mapErrorToUi(err).join(" "), type: "danger" });
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Opens the cancel-confirmation dialog for the create-report flow.
   */
  const handleCancel = () => {
    setShowCancelDialog(true);
  };

  /**
   * Confirms cancellation: closes the dialog and returns to the player detail
   * view. The transient form state lives inside {@link CreateReportView} and is
   * discarded when that view unmounts.
   */
  const confirmCancel = () => {
    setShowCancelDialog(false);
    setCurrentView("player-detail");
  };

  // Matches passed to the create-report view as the report scope: the selected
  // subset in the A1 flow, otherwise all of the player's observed matches.
  const matchesInScope = matchSubsetSelected
    ? playerMatches.filter((m) => selectedMatches.includes(m.id))
    : playerMatches;

  return (
    <div
      className="flex h-screen bg-[#0B0B0F] text-[#F5F5F7]"
      style={{ fontFamily: "Inter, system-ui, sans-serif" }}
    >
      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* Pane A - Player List */}
        <div className="w-[360px] bg-[#0B0B0F] border-r border-[#26262F] flex flex-col overflow-hidden">
          <div className="p-6 border-b border-[#26262F]">
            <h2 className="text-lg font-semibold mb-1">My Players</h2>
            <p className="text-sm text-[#6B6B75]">
              Players you have observed in at least one match
            </p>
          </div>
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {players.map((player) => (
              <PlayerListItem
                key={player.id}
                player={player}
                selected={selectedPlayer?.id === player.id}
                onClick={() => handlePlayerSelect(player)}
              />
            ))}
          </div>
        </div>

        {/* Pane B - Dynamic Content */}
        <div className="flex-1 overflow-y-auto">
          {currentView === "players" &&
            (loadingPlayers ? (
              <EmptyState
                icon="⏳"
                title="Loading players…"
                description="Fetching the players you have observed."
              />
            ) : loadError.length > 0 ? (
              <EmptyState
                icon="⚠️"
                title="Could not load players"
                description={loadError.join(" ")}
              />
            ) : (
              <EmptyState
                icon="🏀"
                title="No observed players yet"
                description="Start by delegating yourself to observe matches, then players will appear here."
              />
            ))}

          {currentView === "player-detail" && selectedPlayer && (
            <PlayerDetailView
              player={selectedPlayer}
              onCreateReport={handleCreateReport}
              onViewMatches={handleViewMatches}
              showE1Error={showE1Error}
            />
          )}

          {currentView === "player-matches" && selectedPlayer && (
            <PlayerMatchesView
              player={selectedPlayer}
              matches={playerMatches}
              selectedMatches={selectedMatches}
              onToggleMatch={handleToggleMatch}
              onCreateFromSelected={handleCreateFromSelectedMatches}
              onBack={() => setCurrentView("player-detail")}
            />
          )}

          {currentView === "create-report" && selectedPlayer && (
            <CreateReportView
              player={selectedPlayer}
              matches={matchesInScope}
              matchSubsetSelected={matchSubsetSelected}
              onSubmit={handleSubmitReport}
              onCancel={handleCancel}
            />
          )}

          {currentView === "report-success" && selectedPlayer && reportResult && (
            <ReportSuccessView
              player={selectedPlayer}
              result={reportResult}
              onBackToPlayer={() => setCurrentView("player-detail")}
            />
          )}
        </div>
      </div>

      {/* Cancel Confirmation Dialog */}
      {showCancelDialog && (
        <Modal
          title="Discard this report?"
          description="Your inputs will be lost. This cannot be undone."
          onConfirm={confirmCancel}
          onCancel={() => setShowCancelDialog(false)}
          confirmLabel="Discard report"
          cancelLabel="Keep editing"
          danger
        />
      )}

      {/* Toast Notification */}
      {toast && <Toast message={toast.message} type={toast.type} />}
    </div>
  );
}
