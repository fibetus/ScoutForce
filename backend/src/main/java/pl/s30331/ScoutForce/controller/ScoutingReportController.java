package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.controller.dto.ScoutingReportDto;
import pl.s30331.ScoutForce.controller.dto.ScoutingReportDtoMapper;
import pl.s30331.ScoutForce.model.DetailedRating;
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
    public ResponseEntity<ScoutingReportDto> createReport(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @RequestBody CreateScoutingReportRequest request) {

        return ResponseEntity.ok(ScoutingReportDtoMapper.toDto(
                scoutingReportService.createScoutingReport(
                        scoutId,
                        playerId,
                        request.getNote(),
                        request.getRecommendation(),
                        request.getDetailedRatings()
                )));
    }

    @PostMapping("/scouts/{scoutId}/players/{playerId}/reports/from-matches")
    public ResponseEntity<ScoutingReportDto> createReportFromSelectedMatches(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @RequestBody CreateScoutingReportFromMatchesRequest request) {

        return ResponseEntity.ok(ScoutingReportDtoMapper.toDto(
                scoutingReportService.createScoutingReportFromSelectedMatches(
                        scoutId,
                        playerId,
                        request.getMatchIds(),
                        request.getNote(),
                        request.getRecommendation(),
                        request.getDetailedRatings()
                )));
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
