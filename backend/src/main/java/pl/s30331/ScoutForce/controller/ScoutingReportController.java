package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.model.ScoutingReport;
import pl.s30331.ScoutForce.service.ScoutingReportService;
import pl.s30331.ScoutForce.service.ViewPlayerMatchesService;
import pl.s30331.ScoutForce.service.ViewPlayersListService;
import pl.s30331.ScoutForce.repository.PlayerRepository;
import pl.s30331.ScoutForce.repository.ScoutRepository;

import java.util.List;

/**
 * REST controller for the primary use case: Create Scouting Report.
 *
 * ─────────────────────────────────────────────────────────────
 * Endpoints supporting the primary use case:
 *
 *  GET  /api/scouts/{scoutId}/players
 *       <<include>> View Players List – players observed by this scout
 *
 *  GET  /api/scouts/{scoutId}/players/{playerId}/matches
 *       <<extend>> View Player's Matches – matches in which player appeared
 *                  AND were observed by the scout
 *
 *  POST /api/scouts/{scoutId}/players/{playerId}/reports
 *       Create Scouting Report (default flow – all observed matches)
 *
 *  POST /api/scouts/{scoutId}/players/{playerId}/reports/from-matches
 *       Create Scouting Report (alternative A1 – selected matches)
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/scouts/{scoutId}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutingReportController {

    /**
     * TEMPORARY: Default scout ID for demo/testing purposes.
     * The DataInitializer seeds the default scout as the second Person entity
     * (Director is first), so its ID = 2 (auto-generated). Replace with proper auth when available.
     */
    public static final Long DEFAULT_SCOUT_ID = 2L; // TODO: replace with auth context

    private final ScoutingReportService    scoutingReportService;
    private final ViewPlayersListService   viewPlayersListService;
    private final ViewPlayerMatchesService viewPlayerMatchesService;
    private final ScoutRepository          scoutRepository;
    private final PlayerRepository         playerRepository;

    // ── <<include>> View Players List ─────────────────────────────────────────

    /**
     * Returns players that the scout has observed (eligible for report creation).
     * Navigation: scout → watchedMatches → matchStats → player
     */
    @GetMapping("/players")
    public ResponseEntity<List<Player>> getObservablePlayers(@PathVariable Long scoutId) {
        Scout scout = scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
        return ResponseEntity.ok(viewPlayersListService.getPlayersObservedByScout(scout));
    }

    // ── <<extend>> View Player's Matches ─────────────────────────────────────

    /**
     * Returns matches in which the player appeared AND were observed by the scout.
     * Navigation: player → matchStats → match ∩ scout → watchedMatches
     */
    @GetMapping("/players/{playerId}/matches")
    public ResponseEntity<List<Match>> getPlayerMatchesForScout(
            @PathVariable Long scoutId,
            @PathVariable Long playerId) {

        Scout scout = scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Player not found: " + playerId));

        List<Match> matches = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout);

        if (matches.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(matches);
    }

    // ── Create Scouting Report (default flow) ────────────────────────────────

    /**
     * POST body: CreateScoutingReportRequest
     * Default flow – all observed matches of the player are included.
     */
    @PostMapping("/players/{playerId}/reports")
    public ResponseEntity<ScoutingReport> createReport(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @RequestBody CreateScoutingReportRequest request) {

        ScoutingReport report = scoutingReportService.createScoutingReport(
                scoutId,
                playerId,
                request.getNote(),
                request.getRecommendation(),
                request.getDetailedRatings()
        );
        return ResponseEntity.ok(report);
    }

    // ── Create Scouting Report from selected matches (A1 flow) ───────────────

    /**
     * POST body: CreateScoutingReportFromMatchesRequest
     * Alternative A1 – scout manually selects a subset of matches.
     */
    @PostMapping("/players/{playerId}/reports/from-matches")
    public ResponseEntity<ScoutingReport> createReportFromSelectedMatches(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @RequestBody CreateScoutingReportFromMatchesRequest request) {

        ScoutingReport report = scoutingReportService.createScoutingReportFromSelectedMatches(
                scoutId,
                playerId,
                request.getMatchIds(),
                request.getNote(),
                request.getRecommendation(),
                request.getDetailedRatings()
        );
        return ResponseEntity.ok(report);
    }

    // ── Stubs for remaining use cases ─────────────────────────────────────────

    /** Stub – not yet implemented */
    @GetMapping("/reports")
    public ResponseEntity<?> getReportsByScout(@PathVariable Long scoutId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /** Stub – not yet implemented */
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long scoutId,
                                          @PathVariable Long reportId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    // ── Inner request DTOs ────────────────────────────────────────────────────

    /** Request body for the default Create Scouting Report flow */
    @lombok.Data
    public static class CreateScoutingReportRequest {
        private String note;
        private pl.s30331.ScoutForce.model.enums.RecommendationType recommendation;
        private List<pl.s30331.ScoutForce.model.DetailedRating> detailedRatings;
    }

    /** Request body for the A1 (selected matches) flow */
    @lombok.Data
    public static class CreateScoutingReportFromMatchesRequest {
        private List<Long> matchIds;
        private String note;
        private pl.s30331.ScoutForce.model.enums.RecommendationType recommendation;
        private List<pl.s30331.ScoutForce.model.DetailedRating> detailedRatings;
    }
}
