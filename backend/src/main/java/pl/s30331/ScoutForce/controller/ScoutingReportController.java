package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.model.ScoutingReport;
import pl.s30331.ScoutForce.service.ScoutingReportService;

import java.util.List;

/**
 * REST controller for the primary use case: Create Scouting Report.
 *
 * ─────────────────────────────────────────────────────────────
 * Endpoints supporting the primary use case:
 *
 *  POST /api/scouts/{scoutId}/players/{playerId}/reports
 *       Create Scouting Report (default flow – all observed matches)
 *
 *  POST /api/scouts/{scoutId}/players/{playerId}/reports/from-matches
 *       Create Scouting Report (alternative A1 – selected matches)
 * ─────────────────────────────────────────────────────────────
 *
 * The read-only player-browsing endpoints (GET .../players and
 * GET .../players/{playerId}/matches) have been relocated to
 * {@link PlayerController}; this controller now focuses solely on
 * report creation (plus the remaining stubs).
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

    private final ScoutingReportService scoutingReportService;

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
