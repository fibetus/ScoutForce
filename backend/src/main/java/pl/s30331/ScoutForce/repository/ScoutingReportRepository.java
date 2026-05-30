package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.ScoutingReport;

import java.util.List;

@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {

    /** All reports authored by a given scout */
    List<ScoutingReport> findByCreatedById(Long scoutId);

    /** All reports for a given player */
    List<ScoutingReport> findByPlayerId(Long playerId);
}
