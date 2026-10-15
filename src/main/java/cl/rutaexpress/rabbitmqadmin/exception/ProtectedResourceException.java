package cl.rutaexpress.rabbitmqadmin.exception;


public class ProtectedResourceException extends RuntimeException {

    public ProtectedResourceException(String message) {
        super(message);
    }
}