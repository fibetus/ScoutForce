package pl.s30331.ScoutForce.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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
 * REST endpoints for the primary use case: <strong>Create Scouting Report</strong>.
 *
 * <p>Both endpoints return a {@link ScoutingReportDto} rather than the JPA entity graph.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutingReportController {

    private final ScoutingReportService scoutingReportService;

    /**
     * Default flow — report covers all matches the scout observed for this player.
     *
     * @param scoutId  authoring scout id
     * @param playerId subject player id
     * @param request  note, recommendation and detailed ratings
     * @return {@code 200 OK} with the saved report as {@link ScoutingReportDto}
     */
    @PostMapping("/scouts/{scoutId}/players/{playerId}/reports")
    public ResponseEntity<ScoutingReportDto> createReport(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @Valid @RequestBody CreateScoutingReportRequest request) {

        return ResponseEntity.ok(ScoutingReportDtoMapper.toDto(
                scoutingReportService.createScoutingReport(
                        scoutId,
                        playerId,
                        request.getNote(),
                        request.getRecommendation(),
                        request.getDetailedRatings()
                )));
    }

    /**
     * Alternative flow A1 — report covers only the selected match ids.
     *
     * @param scoutId  authoring scout id
     * @param playerId subject player id
     * @param request  selected match ids plus report body
     * @return {@code 200 OK} with the saved report as {@link ScoutingReportDto}
     */
    @PostMapping("/scouts/{scoutId}/players/{playerId}/reports/from-matches")
    public ResponseEntity<ScoutingReportDto> createReportFromSelectedMatches(
            @PathVariable Long scoutId,
            @PathVariable Long playerId,
            @Valid @RequestBody CreateScoutingReportFromMatchesRequest request) {

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

    /** JSON body for the default create-report endpoint. */
    @Data
    public static class CreateScoutingReportRequest {
        @NotBlank
        private String note;
        @NotNull
        private RecommendationType recommendation;
        @NotEmpty
        @Valid
        private List<DetailedRating> detailedRatings;
    }

    /** JSON body for the A1 create-report-from-matches endpoint. */
    @Data
    public static class CreateScoutingReportFromMatchesRequest {
        @NotEmpty
        private List<Long> matchIds;
        @NotBlank
        private String note;
        @NotNull
        private RecommendationType recommendation;
        @NotEmpty
        @Valid
        private List<DetailedRating> detailedRatings;
    }
}
