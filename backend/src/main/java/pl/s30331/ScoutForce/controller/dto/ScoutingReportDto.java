package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Web-layer projection of a persisted {@code ScoutingReport}.
 *
 * <p>Exposes scalar fields, the derived {@code finalRating}, and foreign-key ids
 * instead of serializing lazy JPA associations.</p>
 *
 * @param id               report primary key
 * @param createdAt        date the report was authored
 * @param note             free-text evaluation
 * @param recommendation   {@link pl.s30331.ScoutForce.model.enums.RecommendationType} name
 * @param finalRating      weighted average of detailed ratings
 * @param scoutId          authoring scout id
 * @param playerId         subject player id
 * @param detailedRatings  embedded rating lines
 * @param basedOnMatchIds  ids of matches that ground the evaluation
 */
public record ScoutingReportDto(
        Long id,
        LocalDate createdAt,
        String note,
        String recommendation,
        BigDecimal finalRating,
        Long scoutId,
        Long playerId,
        List<DetailedRatingDto> detailedRatings,
        List<Long> basedOnMatchIds
) {
}
