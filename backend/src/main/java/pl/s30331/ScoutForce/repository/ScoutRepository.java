package pl.s30331.ScoutForce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.s30331.ScoutForce.model.Scout;

import java.util.Optional;

@Repository
public interface ScoutRepository extends JpaRepository<Scout, Long> {

    Optional<Scout> findByEmail(String email);

    Optional<Scout> findByLicenseNumber(String licenseNumber);
}
