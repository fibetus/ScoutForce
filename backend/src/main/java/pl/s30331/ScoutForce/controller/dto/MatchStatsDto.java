package pl.s30331.ScoutForce.controller.dto;

/**
 * Web-layer transfer object exposing a single player's box-score for a single match.
 *
 * <p>This is a presentation-layer projection of the {@code MatchStats} association entity. It
 * intentionally exposes only the fields rendered by the prototype's match card (MIN/PTS/REB/AST/
 * STL/BLK); statistics that are not displayed (turnovers, fouls, shooting percentages) are
 * deliberately omitted so the frontend never holds data it does not show.</p>
 *
 * @param minutesPlayed minutes played in the match
 * @param points        points scored in the match
 * @param rebounds      rebounds in the match
 * @param assists       assists in the match
 * @param steals        steals in the match
 * @param blocks        blocks in the match
 */
public record MatchStatsDto(
        Integer minutesPlayed,
        Integer points,
        Integer rebounds,
        Integer assists,
        Integer steals,
        Integer blocks
) {
}
