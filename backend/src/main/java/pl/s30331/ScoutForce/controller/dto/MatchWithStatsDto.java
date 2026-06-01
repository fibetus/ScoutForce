package pl.s30331.ScoutForce.controller.dto;

import java.time.LocalDate;

/**
 * Web-layer transfer object exposing a single match together with one player's box-score for it.
 *
 * <p>This is a presentation-layer projection of the {@code Match} domain entity. It intentionally
 * exposes only the fields rendered by the prototype's match card, avoiding serialization of the
 * entity's lazy associations. The host and guest clubs are projected to {@link ClubDto}, and
 * {@code stats} carries the box-score of exactly the player from the request URL in this match
 * (selected by in-memory association navigation, never via a foreign-key filtering query).</p>
 *
 * @param id         the match identifier
 * @param date       the date the match was played
 * @param place      the venue the match was played at
 * @param hostScore  the final score of the host club
 * @param guestScore the final score of the guest club
 * @param host       the host club details
 * @param guest      the guest club details
 * @param stats      the requested player's box-score for this match
 */
public record MatchWithStatsDto(
        Long id,
        LocalDate date,
        String place,
        Integer hostScore,
        Integer guestScore,
        ClubDto host,
        ClubDto guest,
        MatchStatsDto stats
) {
}
