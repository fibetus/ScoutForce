package pl.s30331.ScoutForce.controller.dto;

import pl.s30331.ScoutForce.model.DetailedRating;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.ScoutingReport;

import java.util.List;

/**
 * Maps {@link ScoutingReport} domain entities to {@link ScoutingReportDto}.
 */
public final class ScoutingReportDtoMapper {

    private ScoutingReportDtoMapper() {
    }

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

    public static List<ScoutingReportDto> toDtoList(List<ScoutingReport> reports) {
        return reports.stream()
                .map(ScoutingReportDtoMapper::toDto)
                .toList();
    }

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
