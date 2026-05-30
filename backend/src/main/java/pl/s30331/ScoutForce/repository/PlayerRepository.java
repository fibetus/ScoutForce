package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.Player;
import pl.s30331.ScoutForce.model.enums.PlayerStatus;
import pl.s30331.ScoutForce.model.enums.PositionType;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByPlayerStatus(PlayerStatus status);

    List<Player> findByPosition(PositionType position);

    List<Player> findByClubId(Long clubId);
}
