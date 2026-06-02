package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.MatchStats;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.repository.PlayerRepository;
import pl.s30331.ScoutForce.repository.ScoutRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for the <<include>> View Players List use case.
 *
 * Returns the list of all players navigable by the scout,
 * with derived /averageRating already populated (lazy-loaded via association).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewPlayersListService {

    private final PlayerRepository playerRepository;
    private final ScoutRepository scoutRepository;

    /**
     * Returns ALL players in the system.
     *
     * <p>This method uses the repository to fetch all player entities. It is primarily used
     * for global browsing by admins or scouts looking for any player.</p>
     *
     * @return a list of all {@link Player} entities
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Fetches a single player by ID.
     *
     * @param playerId the identifier of the player
     * @return the {@link Player} entity
     * @throws jakarta.persistence.EntityNotFoundException if not found
     */
    public Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Player not found: " + playerId
                ));
    }

    /**
     * Fetches a single scout by ID.
     *
     * @param scoutId the identifier of the scout
     * @return the {@link Scout} entity
     * @throws jakarta.persistence.EntityNotFoundException if not found
     */
    public Scout getScout(Long scoutId) {
        return scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
    }

    /**
     * Returns only those players for whom the given scout has observed at least one match.
     *
     * <p>These are the players eligible for a new scouting report by this scout.
     * The logic follows the association-navigation rule:</p>
     * <ol>
     *   <li>Navigate from scout to {@code watchedMatches}.</li>
     *   <li>For each match, navigate to its {@code matchStats}.</li>
     *   <li>From each matchStats entry, navigate to the {@code player}.</li>
     * </ol>
     * <p>The resulting stream is deduplicated and collected. No repository-level filtering
     * is performed.</p>
     *
     * @param scout the scout whose observed players are requested
     * @return a list of unique {@link Player} entities observed by the scout
     */
    public List<Player> getPlayersObservedByScout(Scout scout) {
        return scout.getWatchedMatches().stream()
                .flatMap(match -> match.getMatchStats().stream())
                .map(MatchStats::getPlayer)
                .distinct()
                .collect(Collectors.toList());
    }
}
