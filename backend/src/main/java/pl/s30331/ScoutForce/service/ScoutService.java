package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.repository.ScoutRepository;

/**
 * Loads {@link Scout} aggregate roots by id.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoutService {

    private final ScoutRepository scoutRepository;

    public Scout getScout(Long scoutId) {
        return scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
    }
}
