package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Web-layer projection of a {@code ScoutingReport}.
 *
 * <p>Exposes persisted fields, the derived {@code finalRating}, and identifiers of
 * related aggregates instead of serializing JPA entity graphs.</p>
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
