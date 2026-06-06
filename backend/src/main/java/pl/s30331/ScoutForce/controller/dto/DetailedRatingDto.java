package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;

/**
 * Web-layer projection of a single {@code DetailedRating} within a scouting report.
 */
public record DetailedRatingDto(
        Long id,
        String type,
        BigDecimal rating,
        String comment,
        BigDecimal weight
) {
}
