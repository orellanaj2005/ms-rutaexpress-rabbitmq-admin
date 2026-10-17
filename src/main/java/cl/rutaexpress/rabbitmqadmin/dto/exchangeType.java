package cl.rutaexpress.rabbitmqadmin.dto;


public enum ExchangeType {
    DIRECT,
    TOPIC,
    FANOUT,
    HEADERS;

    /** El Management API de RabbitMQ espera el tipo en minúsculas. */
    public String toRabbitType() {
        return name().toLowerCase();
    }
}
