package cl.keber.infrastructure.web;

import cl.keber.domain.exception.TrainingProgramNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates the exceptions the application layer raises into HTTP status codes
 * (decision D1).
 *
 * <ul>
 *   <li>{@link IllegalArgumentException} - a value object rejected the request body, or
 *       the body id contradicted the path id - becomes {@code 400 Bad Request};
 *   <li>{@link TrainingProgramNotFoundException} becomes {@code 404 Not Found}.
 * </ul>
 *
 * <p>Before this class existed both escaped the dispatcher unhandled and surfaced as
 * {@code 500 Internal Server Error}. The response body deliberately keeps the shape of
 * Spring Boot's default error body - {@code timestamp}, {@code status}, {@code error},
 * {@code message}, {@code path} - so that clients parsing errors see no structural change,
 * only a truthful status code.
 *
 * <p>Deserialization failures are not handled here on purpose: malformed JSON never
 * reaches a controller method, so Jackson's {@code HttpMessageNotReadableException} keeps
 * producing the {@code 400} it always produced.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(TrainingProgramNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            TrainingProgramNotFoundException exception, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, exception, request);
    }

    private static ResponseEntity<Map<String, Object>> body(
            HttpStatus status, RuntimeException exception, HttpServletRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", exception.getMessage());
        payload.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(payload);
    }
}
