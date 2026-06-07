package pl.s30331.ScoutForce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.s30331.ScoutForce.controller.dto.ScoutSummaryDto;
import pl.s30331.ScoutForce.model.Scout;
import pl.s30331.ScoutForce.service.ScoutService;

/**
 * Resolves {@link Scout} identity for clients that cannot hard-code a database id.
 */
@RestController
@RequestMapping("/api/scouts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoutController {

    private final ScoutService scoutService;

    /**
     * Returns the demo scout seeded with license {@link ScoutService#DEFAULT_LICENSE_NUMBER}.
     *
     * @return {@code 200 OK} with id, name and license number
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

    /**
     * Looks up a scout by license number.
     *
     * @param licenseNumber unique scout license (e.g. {@code SCT-001})
     * @return {@code 200 OK} with summary fields, or {@code 404} when unknown
     */
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
}
