package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.ScoutingReport;

/**
 * Spring Data repository for persisting {@link ScoutingReport} aggregate roots.
 *
 * <p>Reports are created exclusively through {@link pl.s30331.ScoutForce.service.ScoutingReportService}
 * after domain validation; no custom finders are defined.</p>
 */
@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {
}
