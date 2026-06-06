package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.service.ScoutService;

/**
 * Resolves scout identity for demo clients that cannot rely on a fixed database id.
 */
@RestController
@RequestMapping("/api/scouts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutController {

    private final ScoutService scoutService;

    /**
     * Returns the demo scout seeded with license {@link ScoutService#DEFAULT_LICENSE_NUMBER}.
     */
    @GetMapping("/default")
    public ResponseEntity<ScoutSummaryDto> getDefaultScout() {
        Scout scout = scoutService.getDefaultScout();
        return ResponseEntity.ok(new ScoutSummaryDto(
                scout.getId(),
                scout.getFirstName(),
                scout.getLastName(),
                scout.getLicenseNumber()
        ));
    }

    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<ScoutSummaryDto> getScoutByLicense(@PathVariable String licenseNumber) {
        Scout scout = scoutService.getScoutByLicenseNumber(licenseNumber);
        return ResponseEntity.ok(new ScoutSummaryDto(
                scout.getId(),
                scout.getFirstName(),
                scout.getLastName(),
                scout.getLicenseNumber()
        ));
    }

    public record ScoutSummaryDto(Long id, String firstName, String lastName, String licenseNumber) {}
}
