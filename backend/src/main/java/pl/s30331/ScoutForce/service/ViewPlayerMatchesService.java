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
 * {@code <<extend>> View Player's Matches} — reads match participation and box-score data for a player.
 *
 * <p>All data is reached by navigating aggregate associations in memory:
 * {@link Player#getMatchStats()} for the player's rows and {@link Scout#getWatchedMatches()} for
 * scout-observed matches. No repository finder filters by foreign key.</p>
 *
 * <p>When both player and scout are in scope, results are the <em>intersection</em> of those two
 * association sets (same match id in both). {@link MatchStats} rows are always taken from
 * {@link Player#getMatchStats()} — each row already belongs to the player, so reverse navigation
 * through {@code match.getMatchStats()} is never used.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewPlayerMatchesService {

    /** Sentinel returned by {@link #aggregate(List)} when the input list is empty. */
    private static final AggregatedBoxScore ZERO_KPI = new AggregatedBoxScore(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    /**
     * Returns matches observed by the scout in which the player also participated.
     *
     * <p>Player match ids are collected from {@link Player#getMatchStats()} → {@link MatchStats#getMatch()};
     * the result is filtered from {@link Scout#getWatchedMatches()}.</p>
     *
     * @param player loaded player aggregate root (typically from {@link PlayerService#getPlayerById(Long)})
     * @param scout  loaded scout aggregate root (typically from {@link ScoutService#getScoutById(Long)})
     * @return intersection of watched and played matches; never {@code null}, may be empty
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
     * Returns every {@link MatchStats} row linked to the player.
     *
     * @param player loaded player aggregate root
     * @return {@link Player#getMatchStats()} (same persistence collection); never {@code null}
     */
    public List<MatchStats> getAllMatchStats(Player player) {
        return player.getMatchStats();
    }

    /**
     * Returns {@link MatchStats} rows for matches the scout observed in which the player played.
     *
     * <p>Filters {@link Player#getMatchStats()} by match ids from
     * {@link #getObservedMatchesForPlayer(Player, Scout)}.</p>
     *
     * @param player loaded player aggregate root
     * @param scout  loaded scout aggregate root
     * @return box-score rows for the intersection; never {@code null}, may be empty
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
     * Finds the player's box-score for a single match.
     *
     * @param player loaded player aggregate root
     * @param match  match to look up (only {@link Match#getId()} is compared)
     * @return the matching {@link MatchStats} row, or empty when {@code match.id} is null or no row exists
     */
    public Optional<MatchStats> getMatchStatsForMatch(Player player, Match match) {
        if (match.getId() == null) {
            return Optional.empty();
        }
        return player.getMatchStats().stream()
                .filter(ms -> ms.getMatch() != null
                        && Objects.equals(ms.getMatch().getId(), match.getId()))
                .findFirst();
    }

    /**
     * Computes global KPI averages over all of the player's {@link MatchStats} rows.
     *
     * @param player loaded player aggregate root
     * @return per-field arithmetic means rounded to one decimal place, or all zeros when the player
     *         has no statistics
     */
    public AggregatedBoxScore computeKpiForPlayer(Player player) {
        return aggregate(getAllMatchStats(player));
    }

    /**
     * Computes KPI averages scoped to scout-observed matches in which the player appeared.
     *
     * @param player loaded player aggregate root
     * @param scout  loaded scout aggregate root
     * @return per-field arithmetic means rounded to one decimal place, or all zeros when the
     *         intersection yields no statistics
     */
    public AggregatedBoxScore computeKpiObservedByScout(Player player, Scout scout) {
        return aggregate(getMatchStatsObservedByScout(player, scout));
    }

    /**
     * Averages each box-score field across the given rows.
     *
     * @param stats non-null list of {@link MatchStats} (may be empty)
     * @return {@link #ZERO_KPI} when {@code stats} is empty, otherwise a new {@link AggregatedBoxScore}
     */
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

    /**
     * Arithmetic mean of a single integer field across {@link MatchStats} rows.
     *
     * @param stats non-empty list of rows
     * @param field getter for the field to average (e.g. {@link MatchStats#getPoints})
     * @return mean rounded to one decimal place ({@link RoundingMode#HALF_UP})
     */
    private BigDecimal average(List<MatchStats> stats, ToIntFunction<MatchStats> field) {
        long sum = stats.stream().mapToInt(field).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(stats.size()), 1, RoundingMode.HALF_UP);
    }

    /**
     * Service-layer result of KPI aggregation — mapped to {@link pl.s30331.ScoutForce.controller.dto.PlayerKpiDto}
     * in the web layer.
     *
     * @param avgMinutes  average minutes played per match
     * @param avgPoints   average points per match
     * @param avgRebounds average rebounds per match
     * @param avgAssists  average assists per match
     * @param avgSteals   average steals per match
     * @param avgBlocks   average blocks per match
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
