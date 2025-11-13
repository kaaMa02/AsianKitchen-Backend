package ch.asiankitchen.config;

import ch.asiankitchen.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    private static Map<String,Object> body(String message, Map<String,?> details) {
        Map<String,Object> b = new HashMap<>();
        b.put("message", message);
        if (details != null && !details.isEmpty()) b.put("details", details);
        return b;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleRSE(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body(body(reason, Map.of("code", reason)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> details = new HashMap<>();
        for (var err : ex.getBindingResult().getAllErrors()) {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            details.put(field, err.getDefaultMessage() == null ? "Invalid" : err.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(body("VALIDATION_FAILED", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,Object>> handleConstraint(ConstraintViolationException ex) {
        Map<String,String> details = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                details.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(body("CONSTRAINT_VIOLATION", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,Object>> handleBadJson(HttpMessageNotReadableException ex) {
        String field = null;
        Throwable cause = ex.getCause();
        if (cause instanceof JsonMappingException jm && !jm.getPath().isEmpty()) {
            JsonMappingException.Reference ref = jm.getPath().get(jm.getPath().size() - 1);
            field = ref.getFieldName();
        }
        String code = (cause instanceof InvalidFormatException) ? "INVALID_FORMAT" : "MALFORMED_BODY";
        log.warn("Bad request payload: {} (field={}, code={})", ex.getMessage(), field, code);

        Map<String,String> details = new HashMap<>();
        details.put("code", code);
        if (field != null) details.put("field", field);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422 helps FE UX
                .body(body("Malformed or invalid request body", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(Map.of("message", ex.getMessage(), "code", "ILLEGAL_ARGUMENT"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Fallback: keep JSON body even on 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleOther(Exception ex) {
        log.error("Unhandled server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message", "Unexpected error",
                        "errorType", ex.getClass().getName(),
                        "error", String.valueOf(ex.getMessage())
                ));
    }
}
