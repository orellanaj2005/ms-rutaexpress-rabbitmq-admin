package cl.rutaexpress.rabbitmqadmin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class globalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> body = errorBody("VALIDATION_ERROR", "Uno o más campos no son válidos");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(resourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(resourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(resourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(resourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("RESOURCE_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(protectedResourceException.class)
    public ResponseEntity<Map<String, Object>> handleProtected(protectedResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("PROTECTED_RESOURCE", ex.getMessage()));
    }

    @ExceptionHandler(rabbitManagementUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUnavailable(rabbitManagementUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody("RABBITMQ_UNAVAILABLE", "RabbitMQ no respondió a la solicitud de administración"));
    }

    
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleRabbitError(HttpStatusCodeException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
    }

    private Map<String, Object> errorBody(String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}

