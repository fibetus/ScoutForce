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
 * REST controller exposing read-only player data scoped to a single scout.
 *
 * <p>Following the project's mandatory "associations, not filtering" rule, every aggregate root is
 * loaded by its identifier and related data is reached by navigating in-memory associations via
 * application services. No foreign-key filtering finders are used.</p>
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
     * Returns the players the given scout has observed (those eligible for a new report).
     *
     * @param scoutId the identifier of the scout whose observed players are requested
     * @return {@code 200 OK} with the list of observed players as {@link PlayerDto}
     */
    @GetMapping("/scouts/{scoutId}/players")
    public ResponseEntity<List<PlayerDto>> getObservablePlayers(@PathVariable Long scoutId) {
        Scout scout = scoutService.findScoutById(scoutId);

        List<PlayerDto> players = viewPlayersListService.getPlayersObservedByScout(scout).stream()
                .map(player -> toPlayerDto(player, scout))
                .toList();

        return ResponseEntity.ok(players);
    }

    /**
     * Returns the matches observed by the scout in which the given player also appeared, each
     * enriched with that player's box-score for the match.
     *
     * @param scoutId  the identifier of the observing scout
     * @param playerId the identifier of the player whose observed matches are requested
     * @return {@code 204 No Content} when the player has no matches observed by the scout, otherwise
     *         {@code 200 OK} with the list of {@link MatchWithStatsDto}
     */
    @GetMapping("/scouts/{scoutId}/players/{playerId}/matches")
    public ResponseEntity<List<MatchWithStatsDto>> getPlayerMatchesForScout(
            @PathVariable Long scoutId,
            @PathVariable Long playerId) {

        Scout scout = scoutService.findScoutById(scoutId);
        Player player = playerService.findPlayerById(playerId);

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
     * @param playerId the identifier of the player requested
     * @return {@code 200 OK} with the player as {@link PlayerDto}
     */
    @GetMapping("/players/{playerId}")
    public ResponseEntity<PlayerDto> getPlayerWithStats(@PathVariable Long playerId) {
        PlayerDto player = toPlayerDto(playerService.findPlayerById(playerId));

        return ResponseEntity.ok(player);
    }

    /**
     * Returns all scouting reports authored for the given player.
     *
     * @param playerId the identifier of the player whose reports are requested
     * @return {@code 204 No Content} when the player has no reports, otherwise
     *         {@code 200 OK} with the list of {@link ScoutingReportDto}s
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

    private ClubDto toClubDto(Club club) {
        return new ClubDto(club.getName(), club.getCity(), club.getLeague());
    }

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

    private MatchStatsDto zeroMatchStats() {
        return new MatchStatsDto(0, 0, 0, 0, 0, 0);
    }

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
