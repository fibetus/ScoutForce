package pl.s30331.ScoutForce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ScoutForce Spring Boot application.
 *
 * <p>Bootstraps component scanning, JPA persistence (H2), REST controllers and
 * the {@link DataInitializer} seed when the database is empty.</p>
 */
@SpringBootApplication
public class ScoutForceApplication {

    /**
     * Starts the embedded web server and Spring application context.
     *
     * @param args command-line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(ScoutForceApplication.class, args);
    }
}
