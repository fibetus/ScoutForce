package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;

/**
 * Web-layer projection of one {@code DetailedRating} line inside a report.
 *
 * @param id      rating primary key (null before persist)
 * @param type    criterion label (e.g. {@code offense})
 * @param rating  score 1–10
 * @param comment explanatory text
 * @param weight  contribution weight 0.0–1.0 (all weights in a report sum to 1.0)
 */
public record DetailedRatingDto(
        Long id,
        String type,
        BigDecimal rating,
        String comment,
        BigDecimal weight
) {
}
