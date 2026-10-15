package cl.rutaexpress.rabbitmqadmin.exception;


public class rabbitManagementUnavailableException extends RuntimeException {

    public rabbitManagementUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}