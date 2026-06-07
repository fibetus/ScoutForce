package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;

/**
 * REST endpoints for read-only player views: observed players list, match history, global player
 * catalogue, and scouting reports.
 *
 * <p>Aggregate roots are loaded by id via application services; related data is reached through
 * in-memory association navigation (never foreign-key filtering finders). KPI and per-match stats
 * are delegated to {@link ViewPlayerMatchesService}.</p>
 *
 * <p>The class is {@link Transactional} with {@code readOnly = true} so lazy associations can be
 * traversed safely within each request.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Transactional(readOnly = true)
public class PlayerController {

    private final PlayerService            playerService;
    private final ScoutService             scoutService;
    private final ViewPlayersListService   viewPlayersListService;
    private final ViewPlayerMatchesService viewPlayerMatchesService;

    /**
     * {@code <<include>> View Players List} — players the scout has observed (eligible for a new report).
     *
     * <p>KPI in each {@link PlayerDto} is scoped to matches both played by the player and observed
     * by the scout ({@link ViewPlayerMatchesService#computeKpiObservedByScout(Player, Scout)}).</p>
     *
     * @param scoutId authoring / viewing scout id
     * @return {@code 200 OK} with observed players as {@link PlayerDto}
     */
    @GetMapping("/scouts/{scoutId}/players")
    public ResponseEntity<List<PlayerDto>> getObservablePlayers(@PathVariable Long scoutId) {
        Scout scout = scoutService.getScoutById(scoutId);

        List<PlayerDto> players = viewPlayersListService.getPlayersObservedByScout(scout).stream()
                .map(player -> toPlayerDto(player, scout))
                .toList();

        return ResponseEntity.ok(players);
    }

    /**
     * {@code <<extend>> View Player's Matches} — scout-observed matches in which the player appeared.
     *
     * <p>Each {@link MatchWithStatsDto} includes the player's box-score for that match, resolved via
     * {@link ViewPlayerMatchesService#findMatchStatsForMatch(Player, Match)}. When no row exists, a
     * zeroed {@link MatchStatsDto} is returned.</p>
     *
     * @param scoutId  observing scout id
     * @param playerId subject player id
     * @return {@code 204 No Content} when the intersection is empty, otherwise {@code 200 OK} with
     *         {@link MatchWithStatsDto} list
     */
    @GetMapping("/scouts/{scoutId}/players/{playerId}/matches")
    public ResponseEntity<List<MatchWithStatsDto>> getPlayerMatchesForScout(
            @PathVariable Long scoutId,
            @PathVariable Long playerId) {

        Scout scout = scoutService.getScoutById(scoutId);
        Player player = playerService.getPlayerById(playerId);

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
     * Global extension — all players in the system with career-wide KPI.
     *
     * @return {@code 200 OK} with every player as {@link PlayerDto}
     */
    @GetMapping("/players")
    public ResponseEntity<List<PlayerDto>> getPlayersWithStats() {
        List<PlayerDto> players = playerService.getAllPlayers().stream()
                .map(this::toPlayerDto)
                .toList();

        return ResponseEntity.ok(players);
    }

    /**
     * Global extension — single player profile with career-wide KPI.
     *
     * @param playerId subject player id
     * @return {@code 200 OK} with the player as {@link PlayerDto}
     */
    @GetMapping("/players/{playerId}")
    public ResponseEntity<PlayerDto> getPlayerWithStats(@PathVariable Long playerId) {
        PlayerDto player = toPlayerDto(playerService.getPlayerById(playerId));

        return ResponseEntity.ok(player);
    }

    /**
     * Returns scouting reports authored for the player.
     *
     * @param playerId subject player id
     * @return {@code 204 No Content} when the player has no reports, otherwise {@code 200 OK} with
     *         {@link ScoutingReportDto} list
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
     * Maps a player to {@link PlayerDto} with KPI scoped to the scout's observed matches.
     *
     * @param player loaded player aggregate root
     * @param scout  loaded scout aggregate root
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
                toKpiDto(viewPlayerMatchesService.computeKpiObservedByScout(player, scout))
        );
    }

    /**
     * Maps a player to {@link PlayerDto} with career-wide KPI over all {@link MatchStats} rows.
     *
     * @param player loaded player aggregate root
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
                toKpiDto(viewPlayerMatchesService.computeKpiForPlayer(player))
        );
    }

    /**
     * Maps a {@link Club} entity to {@link ClubDto}.
     *
     * @param club the player's or match participant's club
     */
    private ClubDto toClubDto(Club club) {
        return new ClubDto(club.getName(), club.getCity(), club.getLeague());
    }

    /**
     * Maps a {@link Match} plus the player's box-score row to {@link MatchWithStatsDto}.
     *
     * @param match  match from the scout–player intersection
     * @param player subject player (stats are resolved from {@link Player#getMatchStats()})
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
                viewPlayerMatchesService.findMatchStatsForMatch(player, match)
                        .map(this::toMatchStatsDto)
                        .orElseGet(this::zeroMatchStats)
        );
    }

    /**
     * Maps a single {@link MatchStats} entity to {@link MatchStatsDto}.
     *
     * @param stats player's box-score row for one match
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
     * Fallback box-score when the player has no {@link MatchStats} row for a match in the intersection.
     */
    private MatchStatsDto zeroMatchStats() {
        return new MatchStatsDto(0, 0, 0, 0, 0, 0);
    }

    /**
     * Maps service-layer {@link ViewPlayerMatchesService.AggregatedBoxScore} to {@link PlayerKpiDto}.
     *
     * @param kpi aggregated averages from {@link ViewPlayerMatchesService}
     */
    private PlayerKpiDto toKpiDto(ViewPlayerMatchesService.AggregatedBoxScore kpi) {
        return new PlayerKpiDto(
                kpi.avgMinutes(),
                kpi.avgPoints(),
                kpi.avgRebounds(),
                kpi.avgAssists(),
                kpi.avgSteals(),
                kpi.avgBlocks()
        );
    }
}
