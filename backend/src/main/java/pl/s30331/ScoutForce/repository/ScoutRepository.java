package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.Scout;

import java.util.Optional;

/**
 * Spring Data repository for the {@link Scout} aggregate root.
 *
 * <p>Only {@code findById} (from {@link JpaRepository}) and {@link #findByLicenseNumber}
 * are used; related data is always reached by navigating in-memory associations.</p>
 */
@Repository
public interface ScoutRepository extends JpaRepository<Scout, Long> {

    /**
     * Finds a scout by unique license number (business key for demo bootstrap).
     *
     * @param licenseNumber value of {@link Scout#getLicenseNumber()}
     * @return the scout when present
     */
    Optional<Scout> findByLicenseNumber(String licenseNumber);
}
