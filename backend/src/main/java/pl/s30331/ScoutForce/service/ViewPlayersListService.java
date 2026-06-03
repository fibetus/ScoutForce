package pl.s30331.ScoutForce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.MatchStats;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <<include>> View Players List — players the scout has observed (eligible for a new report).
 * Expects a loaded {@link Scout}; load it via {@link ScoutService}.
 */
@Service
@Transactional(readOnly = true)
public class ViewPlayersListService {

    public List<Player> getPlayersObservedByScout(Scout scout) {
        return scout.getWatchedMatches().stream()
                .flatMap(match -> match.getMatchStats().stream())
                .map(MatchStats::getPlayer)
                .distinct()
                .collect(Collectors.toList());
    }
}
