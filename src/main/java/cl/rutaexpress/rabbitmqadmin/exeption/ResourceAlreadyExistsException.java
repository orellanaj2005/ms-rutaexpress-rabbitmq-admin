package cl.rutaexpress.rabbitmqadmin.exception;


public class resourceAlreadyExistsException extends RuntimeException {

    public resourceAlreadyExistsException(String message) {
        super(message);
    }
}

