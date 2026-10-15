package cl.rutaexpress.rabbitmqadmin.exception;


public class resourceNotFoundException extends RuntimeException {

    public resourceNotFoundException(String message) {
        super(message);
    }
}
