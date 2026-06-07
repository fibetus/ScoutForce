package pl.s30331.ScoutForce.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Translates unchecked exceptions thrown from controllers and services into
 * consistent JSON error responses for the REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps a missing aggregate root ({@code findById} miss) to HTTP 404.
     *
     * @param ex the {@link EntityNotFoundException} raised by lookup services
     * @return JSON body with {@code status}, {@code error} and {@code message}
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maps Bean Validation failures ({@code @Valid} on request bodies) to HTTP 422.
     *
     * @param ex validation errors from the web layer
     * @return JSON error payload with the first field message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validation failed.");
        return error(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    /**
     * Maps domain rule violations (e.g. E1, empty report fields) to HTTP 422.
     *
     * @param ex the business-rule exception with a user-facing message
     * @return JSON error payload suitable for the frontend toast / banner
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(IllegalStateException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /**
     * Maps invalid arguments (e.g. {@link Match#validateBothTeams()}) to HTTP 400.
     *
     * @param ex the argument validation exception
     * @return JSON error payload
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Fallback handler for unexpected failures; hides internal details from clients.
     *
     * @param ex the uncaught exception (logged by Spring; message not exposed)
     * @return HTTP 500 with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    /**
     * Builds the standard error JSON envelope shared by all handlers.
     *
     * @param status  HTTP status to return
     * @param message human-readable explanation for the client
     * @return {@link ResponseEntity} with a {@code Map} body
     */
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    status.value(),
                "error",     status.getReasonPhrase(),
                "message",   message
        ));
    }
}
