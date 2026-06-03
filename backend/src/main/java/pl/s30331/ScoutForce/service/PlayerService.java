package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.ScoutingReport;
import pl.s30331.ScoutForce.repository.PlayerRepository;

import java.util.List;

/**
 * Loads {@link Player} aggregate roots by id or lists all players.
 * Use-case services and controllers depend on this type instead of {@link PlayerRepository}
 * to avoid duplicating {@code findById} / 404 handling.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Player not found: " + playerId));
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /** Reports via {@link Player#getScoutingReports()} association navigation. */
    public List<ScoutingReport> getScoutingReports(Long playerId) {
        return getPlayer(playerId).getScoutingReports();
    }
}
