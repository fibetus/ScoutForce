package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.repository.ScoutRepository;

/**
 * Loads {@link Scout} aggregate roots by id or license number.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoutService {

    public static final String DEFAULT_LICENSE_NUMBER = "SCT-001";

    private final ScoutRepository scoutRepository;

    public Scout getScout(Long scoutId) {
        return scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
    }

    public Scout getScoutByLicenseNumber(String licenseNumber) {
        return scoutRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found with license: " + licenseNumber));
    }

    public Scout getDefaultScout() {
        return getScoutByLicenseNumber(DEFAULT_LICENSE_NUMBER);
    }
}
