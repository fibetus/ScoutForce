package pl.s30331.ScoutForce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;

import java.util.List;

/**
 * {@code <<include>> View Players List} — returns players directly observed by a scout.
 *
 * <p>Data is obtained exclusively by navigating {@link Scout#getObservedPlayers()};
 * no repository finder filters by foreign key.</p>
 */
@Service
@Transactional(readOnly = true)
public class ViewPlayersListService {

    /**
     * Returns the players the scout has marked as observed.
     *
     * @param scout loaded scout aggregate root (typically from {@link ScoutService#getScout(Long)})
     * @return mutable view of {@link Scout#getObservedPlayers()} (same persistence collection)
     */
    public List<Player> getPlayersObservedByScout(Scout scout) {
        return scout.getObservedPlayers();
    }
}
