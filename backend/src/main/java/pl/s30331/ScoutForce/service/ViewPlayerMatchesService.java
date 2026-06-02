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
 * Service for the <<extend>> View Player's Matches use case.
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

    /**
     * Returns matches observed by the scout in which the player also participated.
     *
     * <p>This method implements the core logic for intersecting two aggregate roots'
     * associations in memory:</p>
     * <ul>
     *   <li>{@code player -> matchStats -> match} (all matches the player played in)</li>
     *   <li>{@code scout -> watchedMatches} (all matches the scout has observed)</li>
     * </ul>
     * <p>The result is the subset of player's matches that are also present in the scout's
     * watched list. No foreign-key filtering queries are used, adhering to the project's
     * association-navigation mandate.</p>
     *
     * @param player the player whose matches are being queried
     * @param scout  the scout whose observations scope the result
     * @return the intersection of matches as a list
     */
    public List<Match> getObservedMatchesForPlayer(Player player, Scout scout) {
        List<Match> playerMatches = player.getMatchStats().stream()
                .map(MatchStats::getMatch)
                .toList();

        return scout.getWatchedMatches().stream()
                .filter(playerMatches::contains)
                .toList();
    }

    /**
     * Returns all matches in which the given player participated.
     *
     * <p>Obtained by navigating the {@link Player#getMatchStats()} association and
     * mapping each entry to its corresponding {@link Match}.</p>
     *
     * @param player the player whose matches are requested
     * @return a list of all matches the player participated in
     */
    public List<Match> getMatchesForPlayer(Player player) {
        List<Match> playerMatches = player.getMatchStats().stream()
                .map(MatchStats::getMatch)
                .toList();

        return playerMatches;
    }
}
