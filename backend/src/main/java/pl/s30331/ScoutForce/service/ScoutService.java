package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.repository.ScoutRepository;

/**
 * Loads {@link Scout} aggregate roots by primary key or license number.
 *
 * <p>Controllers and use-case services depend on this type instead of
 * {@link ScoutRepository} directly so 404 handling stays in one place.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoutService {

    /** License number of the demo scout seeded by {@link pl.s30331.ScoutForce.DataInitializer}. */
    public static final String DEFAULT_LICENSE_NUMBER = "SCT-001";

    private final ScoutRepository scoutRepository;

    /**
     * Loads a scout aggregate root by primary key.
     *
     * @param scoutId primary key of the scout ({@code person_id} in JOINED inheritance)
     * @return the loaded {@link Scout} aggregate root
     * @throws jakarta.persistence.EntityNotFoundException if no scout exists for {@code scoutId}
     */
    public Scout getScoutById(Long scoutId) {
        return scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
    }

    /**
     * Returns a scout by unique license number.
     *
     * @param licenseNumber value of {@link Scout#getLicenseNumber()}
     * @return the matching scout
     * @throws jakarta.persistence.EntityNotFoundException if the license is unknown
     */
    public Scout getScoutByLicenseNumber(String licenseNumber) {
        return scoutRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found with license: " + licenseNumber));
    }

    /**
     * Returns the demo scout used by the frontend on application start.
     *
     * @return scout with license {@link #DEFAULT_LICENSE_NUMBER}
     * @throws jakarta.persistence.EntityNotFoundException if the seed has not run
     */
    public Scout getDefaultScout() {
        return getScoutByLicenseNumber(DEFAULT_LICENSE_NUMBER);
    }
}
