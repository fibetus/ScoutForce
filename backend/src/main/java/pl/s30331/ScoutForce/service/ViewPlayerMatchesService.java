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
     * Navigation:
     *   player → matchStats → match (player's matches)
     *   scout  → watchedMatches     (scout's matches)
     *   result = intersection
     */
    public List<Match> getObservedMatchesForPlayer(Player player, Scout scout) {
        // Matches in which the player appeared
        List<Match> playerMatches = player.getMatchStats().stream()
                .map(MatchStats::getMatch)
                .collect(Collectors.toList());

        // Intersection with what the scout has watched
        return scout.getWatchedMatches().stream()
                .filter(playerMatches::contains)
                .collect(Collectors.toList());
    }
}
