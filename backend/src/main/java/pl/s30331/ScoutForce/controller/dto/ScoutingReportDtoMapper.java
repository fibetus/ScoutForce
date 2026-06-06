package pl.s30331.ScoutForce.controller.dto;

import pl.s30331.ScoutForce.model.DetailedRating;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.ScoutingReport;

import java.util.List;

/**
 * Maps {@link ScoutingReport} domain entities to {@link ScoutingReportDto} for the REST API.
 *
 * <p>Stateless utility — not a Spring bean.</p>
 */
public final class ScoutingReportDtoMapper {

    private ScoutingReportDtoMapper() {
    }

    /**
     * Projects a single persisted report to its DTO shape.
     *
     * @param report managed or detached report with initialized associations needed for mapping
     * @return DTO with ids instead of nested entity graphs
     */
    public static ScoutingReportDto toDto(ScoutingReport report) {
        return new ScoutingReportDto(
                report.getId(),
                report.getCreatedAt(),
                report.getNote(),
                report.getRecommendation().name(),
                report.getFinalRating(),
                report.getCreatedBy().getId(),
                report.getPlayer().getId(),
                report.getDetailedRatings().stream()
                        .map(ScoutingReportDtoMapper::toDetailedRatingDto)
                        .toList(),
                report.getBasedOnMatches().stream()
                        .map(Match::getId)
                        .toList()
        );
    }

    /**
     * Maps a list of reports using {@link #toDto(ScoutingReport)}.
     *
     * @param reports reports obtained via {@link pl.s30331.ScoutForce.model.Player#getScoutingReports()}
     * @return parallel list of DTOs
     */
    public static List<ScoutingReportDto> toDtoList(List<ScoutingReport> reports) {
        return reports.stream()
                .map(ScoutingReportDtoMapper::toDto)
                .toList();
    }

    /**
     * Maps one {@link DetailedRating} entity to {@link DetailedRatingDto}.
     *
     * @param rating rating belonging to a report
     * @return flat DTO copy
     */
    private static DetailedRatingDto toDetailedRatingDto(DetailedRating rating) {
        return new DetailedRatingDto(
                rating.getId(),
                rating.getType(),
                rating.getRating(),
                rating.getComment(),
                rating.getWeight()
        );
    }
}
