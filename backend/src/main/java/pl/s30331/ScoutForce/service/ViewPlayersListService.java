package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Match;
import pl.s30331.ScoutForce.model.MatchStats;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;

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

    private final pl.s30331.ScoutForce.repository.PlayerRepository playerRepository;

    /**
     * Returns ALL players in the system.
     * The controller exposes this as GET /api/players.
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Returns only those players for whom the given scout
     * has observed at least one match (i.e. players eligible for a new report).
     *
     * Navigation: scout → watchedMatches → match.matchStats → player
     */
    public List<Player> getPlayersObservedByScout(Scout scout) {
        return scout.getWatchedMatches().stream()
                .flatMap(match -> match.getMatchStats().stream())
                .map(MatchStats::getPlayer)
                .distinct()
                .collect(Collectors.toList());
    }
}
