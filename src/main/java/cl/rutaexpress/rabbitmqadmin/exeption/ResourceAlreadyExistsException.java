package cl.rutaexpress.rabbitmqadmin.exception;


public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}

