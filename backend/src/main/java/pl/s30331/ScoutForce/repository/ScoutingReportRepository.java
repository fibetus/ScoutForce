package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.ScoutingReport;

@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {
}
