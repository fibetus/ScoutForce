package pl.s30331.ScoutForce.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Web-layer transfer object exposing the player information required by the frontend.
 *
 * <p>This is a presentation-layer projection of the {@code Player} domain entity. It intentionally
 * exposes only the fields consumed by the UI, avoiding serialization of the entity's lazy
 * associations and transient computed values. The {@code position} and {@code playerStatus} enums
 * are exposed as their enum NAME (e.g. {@code "CENTER"}, {@code "OBSERVED"}) so the frontend adapter
 * can map them to its own UI representation.</p>
 *
 * <p>Physical measurements are exposed in metric units (kilograms and centimetres). The
 * {@code averageRating} and {@code kpi} aggregates are computed server-side and provided ready for
 * display, so the frontend performs no domain calculations of its own.</p>
 *
 * @param id            the player identifier
 * @param firstName     the player's first name
 * @param lastName      the player's last name
 * @param birthDate     the player's date of birth
 * @param position      the playing position as the enum NAME (e.g. {@code "CENTER"})
 * @param playerStatus  the scouting status as the enum NAME (e.g. {@code "OBSERVED"})
 * @param weight        the player's weight in kilograms
 * @param height        the player's height in centimetres
 * @param wingspan      the player's wingspan in centimetres
 * @param averageRating the average rating across scouting reports, {@code 0} when none exist
 * @param club          the player's club details
 * @param kpi           the player's aggregated box-score averages over the scout's observed matches
 */
public record PlayerDto(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String position,
        String playerStatus,
        Double weight,
        Double height,
        Double wingspan,
        BigDecimal averageRating,
        ClubDto club,
        PlayerKpiDto kpi
) {
}
