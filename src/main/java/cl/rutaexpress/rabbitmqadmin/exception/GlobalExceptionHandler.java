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

/**
 * Mensajes en español para los cuatro casos que pide la sección 6.2 del
 * documento de distribución: 400 (validación), 404 (no existe), 409 (ya
 * existe o está protegido) y 502 (RabbitMQ no responde).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("RESOURCE_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(ProtectedResourceException.class)
    public ResponseEntity<Map<String, Object>> handleProtected(ProtectedResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("PROTECTED_RESOURCE", ex.getMessage()));
    }

    @ExceptionHandler(RabbitManagementUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUnavailable(RabbitManagementUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody("RABBITMQ_UNAVAILABLE", "RabbitMQ no respondió a la solicitud de administración"));
    }

    /**
     * Cualquier otro error que RabbitMQ devuelva directamente (por ejemplo un
     * 400 por un argumento inconsistente que la validación de este servicio
     * no captura) se reenvía con su propio código y cuerpo en vez de
     * convertirse en un 500 genérico — la misma idea que usa el BFF en
     * DownstreamErrorHandler (documento de Javier, sección 5.2).
     */
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