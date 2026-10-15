package cl.rutaexpress.rabbitmqadmin.exception;


public class RabbitManagementUnavailableException extends RuntimeException {

    public RabbitManagementUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}