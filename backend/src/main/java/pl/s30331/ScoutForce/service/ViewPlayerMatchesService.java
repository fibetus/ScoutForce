package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.MatchStats;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Service for the {@code <<extend>> View Player's Matches} use case and player box-score reads.
 *
 * Returns the intersection of:
 *   – matches in which the given player participated (via MatchStats association)
 *   – matches observed by the given scout (via Scout.watchedMatches association)
 *
 * Navigation is purely association-based, no filtering queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewPlayerMatchesService {

    private static final AggregatedBoxScore ZERO_KPI = new AggregatedBoxScore(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    /**
     * Returns matches observed by the scout in which the player also participated.
     *
     * @param player the player whose matches are being queried
     * @param scout  the scout whose observations scope the result
     * @return the intersection of matches as a list
     */
    public List<Match> getObservedMatchesForPlayer(Player player, Scout scout) {
        Set<Long> playerMatchIds = player.getMatchStats().stream()
                .map(MatchStats::getMatch)
                .filter(Objects::nonNull)
                .map(Match::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return scout.getWatchedMatches().stream()
                .filter(m -> m.getId() != null && playerMatchIds.contains(m.getId()))
                .toList();
    }

    /**
     * Returns every {@link MatchStats} row linked to the player via {@link Player#getMatchStats()}.
     *
     * @param player the player whose statistics are requested
     * @return all box-score rows for the player
     */
    public List<MatchStats> getAllMatchStats(Player player) {
        return player.getMatchStats();
    }

    /**
     * Returns {@link MatchStats} rows for matches the scout observed in which the player played.
     *
     * <p>Scoped by intersecting observed match ids with {@link Player#getMatchStats()} —
     * each row already belongs to the player, so no reverse navigation through
     * {@code match.getMatchStats()} is needed.</p>
     *
     * @param player the player whose statistics are requested
     * @param scout  the scout whose observations scope the result
     * @return box-score rows for the intersection
     */
    public List<MatchStats> getMatchStatsObservedByScout(Player player, Scout scout) {
        Set<Long> observedMatchIds = getObservedMatchesForPlayer(player, scout).stream()
                .map(Match::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return player.getMatchStats().stream()
                .filter(ms -> ms.getMatch() != null
                        && ms.getMatch().getId() != null
                        && observedMatchIds.contains(ms.getMatch().getId()))
                .toList();
    }

    /**
     * Finds the player's box-score for a specific match via {@link Player#getMatchStats()}.
     *
     * @param player the player whose statistics are requested
     * @param match  the match to look up
     * @return the matching row, if present
     */
    public Optional<MatchStats> findMatchStatsForMatch(Player player, Match match) {
        if (match.getId() == null) {
            return Optional.empty();
        }
        return player.getMatchStats().stream()
                .filter(ms -> ms.getMatch() != null
                        && Objects.equals(ms.getMatch().getId(), match.getId()))
                .findFirst();
    }

    /**
     * Averages box-score fields over all of the player's {@link MatchStats} rows.
     *
     * @param player the player whose global KPI is computed
     * @return averaged values, or zeros when the player has no statistics
     */
    public AggregatedBoxScore computeKpiForPlayer(Player player) {
        return aggregate(getAllMatchStats(player));
    }

    /**
     * Averages box-score fields over the player's statistics in scout-observed matches.
     *
     * @param player the player whose KPI is computed
     * @param scout  the scout whose observations scope the aggregation
     * @return averaged values, or zeros when no matching statistics exist
     */
    public AggregatedBoxScore computeKpiObservedByScout(Player player, Scout scout) {
        return aggregate(getMatchStatsObservedByScout(player, scout));
    }

    private AggregatedBoxScore aggregate(List<MatchStats> stats) {
        if (stats.isEmpty()) {
            return ZERO_KPI;
        }
        return new AggregatedBoxScore(
                average(stats, MatchStats::getMinutesPlayed),
                average(stats, MatchStats::getPoints),
                average(stats, MatchStats::getRebounds),
                average(stats, MatchStats::getAssists),
                average(stats, MatchStats::getSteals),
                average(stats, MatchStats::getBlocks)
        );
    }

    private BigDecimal average(List<MatchStats> stats, ToIntFunction<MatchStats> field) {
        long sum = stats.stream().mapToInt(field).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(stats.size()), 1, RoundingMode.HALF_UP);
    }

    /**
     * Averaged box-score values computed from a player's {@link MatchStats} rows.
     *
     * @param avgMinutes  average minutes played
     * @param avgPoints   average points
     * @param avgRebounds average rebounds
     * @param avgAssists  average assists
     * @param avgSteals   average steals
     * @param avgBlocks   average blocks
     */
    public record AggregatedBoxScore(
            BigDecimal avgMinutes,
            BigDecimal avgPoints,
            BigDecimal avgRebounds,
            BigDecimal avgAssists,
            BigDecimal avgSteals,
            BigDecimal avgBlocks
    ) {
    }
}
