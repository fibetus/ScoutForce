package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;

/**
 * Web-layer transfer object carrying a player's aggregated box-score averages (KPI).
 *
 * <p>These averages are computed server-side over the matches observed by the scout in which the
 * player appeared, using in-memory navigation over the aggregate root's associations (never via a
 * foreign-key filtering query). The frontend only displays these values and performs no domain
 * calculations of its own.</p>
 *
 * @param avgMinutes  average minutes played per match
 * @param avgPoints   average points scored per match
 * @param avgRebounds average rebounds per match
 * @param avgAssists  average assists per match
 * @param avgSteals   average steals per match
 * @param avgBlocks   average blocks per match
 */
public record PlayerKpiDto(
        BigDecimal avgMinutes,
        BigDecimal avgPoints,
        BigDecimal avgRebounds,
        BigDecimal avgAssists,
        BigDecimal avgSteals,
        BigDecimal avgBlocks
) {
}
