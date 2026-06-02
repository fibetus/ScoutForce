package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.model.DetailedRating;
import pl.s30331.ScoutForce.model.ScoutingReport;
import pl.s30331.ScoutForce.model.enums.RecommendationType;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutingReportController {

    /**
     * TEMPORARY: Default scout ID for demo/testing purposes.
     */
    public static final Long DEFAULT_SCOUT_ID = 2L;

    private final ScoutingReportService scoutingReportService;

    // ── Create Scouting Report (default flow) ────────────────────────────────

    /**
     * Creates and persists a new scouting report using the default flow.
     *
     * <p>In the default flow, the report is automatically based on ALL matches of the player that
     * the scout has observed. The request body carries the subjective part of the report (note,
     * recommendation, and detailed ratings for specific attributes).</p>
     *
     * @param scoutId  the identifier of the scout creating the report
     * @param playerId the identifier of the player being scouted
     * @param request  the request body containing note, recommendation, and detailed ratings
     * @return {@code 200 OK} with the persisted {@link ScoutingReport}
     * @throws IllegalStateException if the player has no matches observed by the scout or if
     *         validation rules (e.g. weights summing to 1.0) are violated
     * @throws jakarta.persistence.EntityNotFoundException if no scout or player exists for the given ids
     */
    @PostMapping("/scouts/{scoutId}/players/{playerId}/reports")
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


    /**
     * Creates and persists a new scouting report using the Alternative Flow A1 (selected matches).
     *
     * <p>In this flow, the scout manually selects a subset of their observed matches to base the
     * report on. The request body must include the list of match identifiers. Validation ensures
     * that every provided match ID has indeed been observed by the scout.</p>
     *
     * @param scoutId  the identifier of the scout creating the report
     * @param playerId the identifier of the player being scouted
     * @param request  the request body containing match IDs, note, recommendation, and detailed ratings
     * @return {@code 200 OK} with the persisted {@link ScoutingReport}
     * @throws IllegalStateException if any selected match was not observed by the scout or if
     *         validation rules are violated
     * @throws jakarta.persistence.EntityNotFoundException if no scout or player exists for the given ids
     */
    @PostMapping("/scouts/{scoutId}/players/{playerId}/reports/from-matches")
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

    /**
     * Stub for retrieving all reports authored by a specific scout.
     *
     * @param scoutId the identifier of the scout
     * @return currently throws {@link UnsupportedOperationException}
     */
    @GetMapping("/scouts/{scoutId}/reports")
    public ResponseEntity<?> getReportsByScout(@PathVariable Long scoutId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Stub for deleting a scouting report.
     *
     * @param scoutId  the identifier of the scout (security check)
     * @param reportId the identifier of the report to delete
     * @return currently throws {@link UnsupportedOperationException}
     */
    @DeleteMapping("/scouts/{scoutId}/reports/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long scoutId,
                                          @PathVariable Long reportId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    // ── Inner request DTOs ────────────────────────────────────────────────────

    /**
     * Request body for the default Create Scouting Report flow.
     *
     * <p>Contains the subjective data entered by the scout. The match scope is determined
     * automatically by the server as all observed matches.</p>
     */
    @lombok.Data
    public static class CreateScoutingReportRequest {
        /** Free-text observation note about the player. */
        private String note;
        /** The scout's recommendation (e.g. HIGHLY_RECOMMENDED, WATCH, etc.). */
        private RecommendationType recommendation;
        /** List of detailed ratings for specific skill categories. */
        private List<DetailedRating> detailedRatings;
    }

    /**
     * Request body for the A1 (selected matches) flow.
     *
     * <p>Extends the default request by requiring a explicit list of match identifiers that
     * the report should be based on.</p>
     */
    @lombok.Data
    public static class CreateScoutingReportFromMatchesRequest {
        /** The specific subset of match IDs to base the report on. */
        private List<Long> matchIds;
        /** Free-text observation note about the player. */
        private String note;
        /** The scout's recommendation. */
        private RecommendationType recommendation;
        /** List of detailed ratings for specific skill categories. */
        private List<DetailedRating> detailedRatings;
    }
}
