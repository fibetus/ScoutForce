package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.ScoutingReport;
import pl.s30331.ScoutForce.repository.PlayerRepository;

import java.util.List;

/**
 * Loads {@link Player} aggregate roots and exposes related data through association navigation.
 *
 * <p>Use-case services and controllers depend on this type instead of
 * {@link PlayerRepository} to avoid duplicating {@code findById} / 404 handling.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    /**
     * Loads a player aggregate root by primary key.
     *
     * @param playerId primary key of the player
     * @return the loaded {@link Player} aggregate root
     * @throws jakarta.persistence.EntityNotFoundException if no player exists for {@code playerId}
     */
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Player not found: " + playerId));
    }

    /**
     * Returns every player in the system (global extension of the players list).
     *
     * @return all persisted players; never {@code null}
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Returns scouting reports for a player via {@link Player#getScoutingReports()}.
     *
     * @param playerId the player whose reports are requested
     * @return reports linked to the player (may be empty)
     * @throws jakarta.persistence.EntityNotFoundException if the player does not exist
     */
    public List<ScoutingReport> getScoutingReports(Long playerId) {
        return getPlayerById(playerId).getScoutingReports();
    }
}
