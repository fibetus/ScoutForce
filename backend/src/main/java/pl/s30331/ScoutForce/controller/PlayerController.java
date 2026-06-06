package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.controller.dto.ClubDto;
import pl.s30331.ScoutForce.controller.dto.MatchStatsDto;
import pl.s30331.ScoutForce.controller.dto.MatchWithStatsDto;
import pl.s30331.ScoutForce.controller.dto.PlayerDto;
import pl.s30331.ScoutForce.controller.dto.PlayerKpiDto;
import pl.s30331.ScoutForce.controller.dto.ScoutingReportDto;
import pl.s30331.ScoutForce.controller.dto.ScoutingReportDtoMapper;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.service.PlayerService;
import pl.s30331.ScoutForce.service.ScoutService;
import pl.s30331.ScoutForce.service.ViewPlayerMatchesService;
import pl.s30331.ScoutForce.service.ViewPlayersListService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * REST controller exposing read-only player data scoped to a single scout.
 *
 * <p>This controller owns the player-browsing endpoints that were relocated from
 * {@code ScoutingReportController}: viewing the list of players a scout has observed and (added in a
 * later task) viewing a player's observed matches. The request mapping, response codes and contract
 * remain identical after the relocation; only the response shape changes from raw entities to
 * web-layer DTOs.</p>
 *
 * <p>Following the project's mandatory "associations, not filtering" rule, every aggregate root is
 * loaded by its identifier via {@code findById} and all related data is reached by navigating the
 * root's in-memory associations. No foreign-key filtering finders are used.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService            playerService;
    private final ScoutService             scoutService;
    private final ViewPlayersListService   viewPlayersListService;
    private final ViewPlayerMatchesService viewPlayerMatchesService;

    /**
     * Returns the players the given scout has observed (those eligible for a new report).
     *
     * <p>The {@link Scout} aggregate root is loaded by id; the observed players are then obtained by
     * navigating its associations through {@link ViewPlayersListService}. Each {@link Player} entity
     * is mapped to a {@link PlayerDto} for the frontend.</p>
     *
     * @param scoutId the identifier of the scout whose observed players are requested
     * @return {@code 200 OK} with the list of observed players as {@link PlayerDto}
     * @throws jakarta.persistence.EntityNotFoundException if no scout exists for the given id (mapped to {@code 404})
     */
    @GetMapping("/scouts/{scoutId}/players")
    public ResponseEntity<List<PlayerDto>> getObservablePlayers(@PathVariable Long scoutId) {
        Scout scout = scoutService.getScout(scoutId);

        List<PlayerDto> players = viewPlayersListService.getPlayersObservedByScout(scout).stream()
                .map(player -> toPlayerDto(player, scout))
                .toList();

        return ResponseEntity.ok(players);
    }

    /**
     * Returns the matches observed by the scout in which the given player also appeared, each
     * enriched with that player's box-score for the match.
     *
     * <p>Both the {@link Scout} and {@link Player} aggregate roots are loaded by id; the match
     * intersection is obtained from
     * {@link ViewPlayerMatchesService#getObservedMatchesForPlayer(Player, Scout)} (which navigates
     * {@code scout.getWatchedMatches()} and the player's matches via {@code player.getMatchStats()}).
     * When the intersection is empty the endpoint preserves its pre-relocation behaviour and returns
     * {@code 204 No Content} with an empty body; otherwise it returns {@code 200 OK} with the list of
     * {@link MatchWithStatsDto}.</p>
     *
     * @param scoutId  the identifier of the observing scout
     * @param playerId the identifier of the player whose observed matches are requested
     * @return {@code 204 No Content} when the player has no matches observed by the scout, otherwise
     *         {@code 200 OK} with the matches as {@link MatchWithStatsDto}
     * @throws jakarta.persistence.EntityNotFoundException if no scout or player exists for the given
     *         ids (mapped to {@code 404})
     */
    @GetMapping("/scouts/{scoutId}/players/{playerId}/matches")
    public ResponseEntity<List<MatchWithStatsDto>> getPlayerMatchesForScout(
            @PathVariable Long scoutId,
            @PathVariable Long playerId) {

        Scout scout = scoutService.getScout(scoutId);
        Player player = playerService.getPlayer(playerId);

        List<Match> matches = viewPlayerMatchesService.getObservedMatchesForPlayer(player, scout);

        if (matches.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<MatchWithStatsDto> body = matches.stream()
                .map(match -> toMatchWithStatsDto(match, player))
                .toList();

        return ResponseEntity.ok(body);
    }

    /**
     * Returns a list of all players in the system, enriched with their global statistics.
     *
     * <p>This endpoint loads all {@link Player} entities via {@link PlayerService#getAllPlayers()}
     * and maps them to {@link PlayerDto}s. Global statistics (KPIs) are computed across all matches
     * the player has participated in.</p>
     *
     * @return {@code 200 OK} with the list of all players as {@link PlayerDto}
     */
    @GetMapping("/players")
    public ResponseEntity<List<PlayerDto>> getPlayersWithStats() {
        List<PlayerDto> players = playerService.getAllPlayers().stream()
                .map(this::toPlayerDto)
                .toList();

        return ResponseEntity.ok(players);
    }

    /**
     * Returns a single player by ID, enriched with their global statistics.
     *
     * <p>The {@link Player} aggregate root is loaded by id; global statistics (KPIs) are computed
     * across all matches the player has participated in.</p>
     *
     * @param playerId the identifier of the player requested
     * @return {@code 200 OK} with the player as {@link PlayerDto}
     * @throws jakarta.persistence.EntityNotFoundException if no player exists for the given id (mapped to {@code 404})
     */
    @GetMapping("/players/{playerId}")
    public ResponseEntity<PlayerDto> getPlayerWithStats(@PathVariable Long playerId) {
        PlayerDto player = toPlayerDto(playerService.getPlayer(playerId));

        return ResponseEntity.ok(player);
    }

    /**
     * Returns all scouting reports authored for the given player.
     *
     * <p>Reports are obtained by navigating the {@link Player#getScoutingReports()} association.
     * If no reports exist, the endpoint returns {@code 204 No Content}.</p>
     *
     * @param playerId the identifier of the player whose reports are requested
     * @return {@code 204 No Content} when the player has no reports, otherwise
     *         {@code 200 OK} with the list of {@link ScoutingReportDto}s
     * @throws jakarta.persistence.EntityNotFoundException if no player exists for the given id (mapped to {@code 404})
     */
    @GetMapping("/players/{playerId}/reports")
    public ResponseEntity<List<ScoutingReportDto>> getPlayerReports(@PathVariable Long playerId) {
        List<ScoutingReportDto> reports = ScoutingReportDtoMapper.toDtoList(
                playerService.getScoutingReports(playerId));

        if (reports.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reports);
    }


    /**
     * Maps a {@link Player} entity to its web-layer {@link PlayerDto} representation,
     * scoped to a specific scout's observations.
     *
     * <p>Basic fields are copied directly, the club is projected to a {@link ClubDto}, and the
     * {@code position}/{@code playerStatus} enums are exposed as their enum NAME. The
     * {@code averageRating} is the derived value from {@link Player#getAverageRating()} ({@code 0}
     * when the player has no reports). The {@code kpi} averages are computed server-side by
     * {@link #computePlayerKpi(Player, Scout)} via association navigation over the scout's observed
     * matches.</p>
     *
     * @param player the player entity to map
     * @param scout  the scout whose observed matches scope the KPI aggregation
     * @return the corresponding {@link PlayerDto}
     */
    private PlayerDto toPlayerDto(Player player, Scout scout) {
        return new PlayerDto(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getBirthDate(),
                player.getPosition().name(),
                player.getPlayerStatus().name(),
                player.getWeight(),
                player.getHeight(),
                player.getWingspan(),
                player.getAverageRating(),
                toClubDto(player.getClub()),
                computePlayerKpi(player, scout)
        );
    }

    /**
     * Maps a {@link Player} entity to its web-layer {@link PlayerDto} representation,
     * using global statistics.
     *
     * <p>Basic fields are copied directly, the club is projected to a {@link ClubDto}, and the
     * {@code position}/{@code playerStatus} enums are exposed as their enum NAME. The
     * {@code averageRating} is the derived value from {@link Player#getAverageRating()} ({@code 0}
     * when the player has no reports). The {@code kpi} averages are computed server-side by
     * {@link #computePlayerKpi(Player)} over all matches the player appeared in.</p>
     *
     * @param player the player entity to map
     * @return the corresponding {@link PlayerDto}
     */
    private PlayerDto toPlayerDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getBirthDate(),
                player.getPosition().name(),
                player.getPlayerStatus().name(),
                player.getWeight(),
                player.getHeight(),
                player.getWingspan(),
                player.getAverageRating(),
                toClubDto(player.getClub()),
                computePlayerKpi(player)
        );
    }

    /**
     * Maps a {@link Club} entity to its web-layer {@link ClubDto} representation.
     *
     * @param club the club entity to map
     * @return the corresponding {@link ClubDto}
     */
    private ClubDto toClubDto(Club club) {
        return new ClubDto(club.getName(), club.getCity(), club.getLeague());
    }

    /**
     * Maps a {@link Match} entity to a {@link MatchWithStatsDto} for the given player.
     *
     * <p>Basic match fields are copied directly, the host and guest clubs are projected to
     * {@link ClubDto}, and the {@code stats} field carries the box-score of exactly the player from
     * the request URL, selected via association navigation (see
     * {@link #findPlayerMatchStats(Match, Player)}). No foreign-key filtering query is used.</p>
     *
     * @param match  the match entity to map
     * @param player the player whose box-score should be embedded in the result
     * @return the corresponding {@link MatchWithStatsDto}
     */
    private MatchWithStatsDto toMatchWithStatsDto(Match match, Player player) {
        return new MatchWithStatsDto(
                match.getId(),
                match.getDate(),
                match.getPlace(),
                match.getHostScore(),
                match.getGuestScore(),
                toClubDto(match.getHost()),
                toClubDto(match.getGuest()),
                findPlayerMatchStats(match, player)
        );
    }

    /**
     * Selects the given player's box-score for a match and projects it to a {@link MatchStatsDto}.
     *
     * <p>The relevant {@link MatchStats} entry is found by navigating {@code match.getMatchStats()}
     * in memory and matching the entry that belongs to the player (via {@link #belongsToPlayer}),
     * never through a foreign-key filtering finder. The match intersection that produced this match
     * guarantees the player participated, so an entry is expected to exist; should it be absent, a
     * zero-valued {@link MatchStatsDto} is returned defensively instead of failing.</p>
     *
     * @param match  the match whose statistics are inspected
     * @param player the player whose box-score is requested
     * @return the player's {@link MatchStatsDto} for this match, or a zero-valued one if absent
     */
    private MatchStatsDto findPlayerMatchStats(Match match, Player player) {
        return match.getMatchStats().stream()
                .filter(stats -> belongsToPlayer(stats, player))
                .findFirst()
                .map(this::toMatchStatsDto)
                .orElseGet(this::zeroMatchStats);
    }

    /**
     * Maps a {@link MatchStats} entity to its web-layer {@link MatchStatsDto} representation.
     *
     * <p>Only the box-score fields rendered by the prototype (MIN/PTS/REB/AST/STL/BLK) are exposed;
     * other statistics held by the entity are intentionally omitted.</p>
     *
     * @param stats the match-statistics entry to map
     * @return the corresponding {@link MatchStatsDto}
     */
    private MatchStatsDto toMatchStatsDto(MatchStats stats) {
        return new MatchStatsDto(
                stats.getMinutesPlayed(),
                stats.getPoints(),
                stats.getRebounds(),
                stats.getAssists(),
                stats.getSteals(),
                stats.getBlocks()
        );
    }

    /**
     * Builds a {@link MatchStatsDto} with all box-score fields set to zero.
     *
     * <p>Used as a defensive fallback when a match in the intersection unexpectedly has no statistics
     * entry for the player, so the response stays well-formed instead of failing.</p>
     *
     * @return a zero-valued {@link MatchStatsDto}
     */
    private MatchStatsDto zeroMatchStats() {
        return new MatchStatsDto(0, 0, 0, 0, 0, 0);
    }

    /**
     * Computes a player's box-score KPI averages over the matches observed by the given scout.
     *
     * <p>Strictly follows the project's "associations, not filtering" rule: the related data is
     * reached only by navigating in-memory associations from the loaded aggregate roots, never via a
     * foreign-key filtering finder. Concretely it reuses
     * {@link ViewPlayerMatchesService#getObservedMatchesForPlayer(Player, Scout)} (which intersects
     * {@code scout.getWatchedMatches()} with the player's matches obtained from
     * {@code player.getMatchStats()}), then navigates {@code match.getMatchStats()} and keeps only
     * the entries belonging to the given player. The six box-score fields (minutes, points,
     * rebounds, assists, steals, blocks) are then averaged purely in memory.</p>
     *
     * <p>When the player has no matching statistics in the scout's observed matches, all averages
     * are returned as zero (see {@link #zeroKpi()}), avoiding any division by zero.</p>
     *
     * @param player the player whose KPI averages are computed
     * @param scout  the scout whose observed matches scope the aggregation
     * @return a {@link PlayerKpiDto} with the averaged box-score values
     */
    private PlayerKpiDto computePlayerKpi(Player player, Scout scout) {
        List<MatchStats> playerStats = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout).stream()
                .flatMap(match -> match.getMatchStats().stream())
                .filter(stats -> belongsToPlayer(stats, player))
                .toList();

        return averageForEachStat(playerStats);
    }

    /**
     * Computes a player's global box-score KPI averages over all matches they participated in.
     *
     * <p>This follows the same in-memory aggregation logic as {@link #computePlayerKpi(Player, Scout)},
     * but without scoping to a scout's observations. All {@link MatchStats} associated with the
     * player are navigated to and averaged.</p>
     *
     * @param player the player whose global KPI averages are computed
     * @return a {@link PlayerKpiDto} with the averaged box-score values
     */
    private PlayerKpiDto computePlayerKpi(Player player) {
        List<MatchStats> playerStats = viewPlayerMatchesService
                .getMatchesForPlayer(player).stream()
                .flatMap(match -> match.getMatchStats().stream())
                .filter(stats -> belongsToPlayer(stats, player))
                .toList();

        return averageForEachStat(playerStats);
    }

    /**
     * Tells whether a {@link MatchStats} entry belongs to the given player.
     *
     * <p>Identity is compared by the player's primary key, which is robust against lazy proxies that
     * may back the {@code MatchStats.player} association. This is an in-memory comparison over a
     * collection already obtained by association navigation, not a database filter.</p>
     *
     * @param stats  the match-statistics entry to inspect
     * @param player the player to match against
     * @return {@code true} if the entry's player is the given player
     */
    private boolean belongsToPlayer(MatchStats stats, Player player) {
        return stats.getPlayer() != null
                && Objects.equals(stats.getPlayer().getId(), player.getId());
    }

    /**
     * Averages a single integer box-score field over the given match-statistics entries.
     *
     * <p>The caller guarantees the list is non-empty, so no division-by-zero check is needed here.
     * The result is rounded to one decimal place ({@link RoundingMode#HALF_UP}), matching the
     * single-decimal precision the frontend uses for display.</p>
     *
     * @param stats the non-empty match-statistics entries to average
     * @param field accessor for the integer box-score field to aggregate
     * @return the average of the selected field as a {@link BigDecimal} scaled to one decimal
     */
    private BigDecimal average(List<MatchStats> stats, ToIntFunction<MatchStats> field) {
        long sum = stats.stream().mapToInt(field).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(stats.size()), 1, RoundingMode.HALF_UP);
    }

    /**
     * Builds a {@link PlayerKpiDto} with all averages set to zero.
     *
     * <p>Used when the player has no statistics in the scout's observed matches, so that the KPI
     * payload is well-defined and never triggers a division by zero.</p>
     *
     * @return a zero-valued {@link PlayerKpiDto}
     */
    private PlayerKpiDto zeroKpi() {
        return new PlayerKpiDto(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Aggregates all six box-score statistics into a {@link PlayerKpiDto} by averaging them.
     *
     * <p>If the input list is empty, a zero-valued KPI is returned to avoid division by zero.</p>
     *
     * @param playerStats the list of match statistics to aggregate
     * @return a {@link PlayerKpiDto} containing the averages for all stats
     */
    private PlayerKpiDto averageForEachStat(List<MatchStats> playerStats) {
        if (playerStats.isEmpty()) {
            return zeroKpi();
        }

        return new PlayerKpiDto(
                average(playerStats, MatchStats::getMinutesPlayed),
                average(playerStats, MatchStats::getPoints),
                average(playerStats, MatchStats::getRebounds),
                average(playerStats, MatchStats::getAssists),
                average(playerStats, MatchStats::getSteals),
                average(playerStats, MatchStats::getBlocks)
        );
    }
}
