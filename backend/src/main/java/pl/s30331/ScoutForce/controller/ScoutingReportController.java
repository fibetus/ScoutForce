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
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutingReportController {

    private final ScoutingReportService scoutingReportService;

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

    @lombok.Data
    public static class CreateScoutingReportRequest {
        private String note;
        private RecommendationType recommendation;
        private List<DetailedRating> detailedRatings;
    }

    @lombok.Data
    public static class CreateScoutingReportFromMatchesRequest {
        private List<Long> matchIds;
        private String note;
        private RecommendationType recommendation;
        private List<DetailedRating> detailedRatings;
    }
}
