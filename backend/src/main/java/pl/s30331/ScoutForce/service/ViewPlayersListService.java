package pl.s30331.ScoutForce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.Scout;

import java.util.List;

/**
 * {@code <<include>> View Players List} — players directly observed by the scout.
 */
@Service
@Transactional(readOnly = true)
public class ViewPlayersListService {

    public List<Player> getPlayersObservedByScout(Scout scout) {
        return scout.getObservedPlayers();
    }
}
