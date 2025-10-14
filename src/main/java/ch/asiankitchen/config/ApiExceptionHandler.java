package ch.asiankitchen.config;

import ch.asiankitchen.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> details = new HashMap<>();
        for (var err : ex.getBindingResult().getAllErrors()) {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            details.put(field, err.getDefaultMessage());
        }
        Map<String,Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("details", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,Object>> handleConstraintViolations(ConstraintViolationException ex) {
        Map<String,Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("details", ex.getConstraintViolations().stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getPropertyPath().toString(), v.getMessage()), HashMap::putAll));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("message", "Invalid data (conflict or missing required field).");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleOther(Exception ex) {
        Map<String,String> body = new HashMap<>();
        body.put("message", "Unexpected error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
