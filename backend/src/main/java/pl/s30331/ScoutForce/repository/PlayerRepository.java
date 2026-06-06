package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.Player;

/**
 * Spring Data repository for the {@link Player} aggregate root.
 *
 * <p>Provides only generic CRUD; use-case code loads players by id and navigates
 * {@link Player#getMatchStats()}, {@link Player#getScoutingReports()}, etc.</p>
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
