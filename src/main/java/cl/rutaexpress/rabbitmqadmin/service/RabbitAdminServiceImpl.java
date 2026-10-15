package cl.rutaexpress.rabbitmqadmin.service;

import cl.rutaexpress.rabbitmqadmin.config.rabbitAdminProperties;
import cl.rutaexpress.rabbitmqadmin.dto.createBindingRequest;
import cl.rutaexpress.rabbitmqadmin.dto.createExchangeRequest;
import cl.rutaexpress.rabbitmqadmin.dto.createQueueRequest;
import cl.rutaexpress.rabbitmqadmin.exception.ProtectedResourceException;
import cl.rutaexpress.rabbitmqadmin.exception.RabbitManagementUnavailableException;
import cl.rutaexpress.rabbitmqadmin.exception.ResourceAlreadyExistsException;
import cl.rutaexpress.rabbitmqadmin.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;



@Service
public class RabbitAdminServiceImpl implements RabbitAdminService {

    /** Vhost por defecto de RabbitMQ, siempre codificado como %2F en las URL del Management API. */
    private static final String VHOST_ENCODED = "%2F";
    private static final String AMQ_RESERVED_PREFIX = "amq.";

    private final RestTemplate restTemplate;
    private final rabbitAdminProperties properties;
    private final String managementUrl;

    public RabbitAdminServiceImpl(
            @Qualifier("rabbitRestTemplate") RestTemplate restTemplate,
            rabbitAdminProperties properties,
            @Value("${rabbitmq.management-url}") String managementUrl) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.managementUrl = managementUrl;
    }

    // ---------------------------------------------------------------- queues

    @Override
    public Object listQueues() {
        return get("/api/queues");
    }

    @Override
    public Object getQueue(String name) {
        try {
            return get("/api/queues/" + VHOST_ENCODED + "/" + name);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe la cola '" + name + "'");
        }
    }

    @Override
    public void createQueue(createQueueRequest request) {
        rejectIfAmqPrefixed(request.name());

        if (queueExists(request.name())) {
            throw new ResourceAlreadyExistsException("Ya existe una cola llamada '" + request.name() + "'");
        }

        Map<String, Object> arguments = new HashMap<>();
        if (request.deadLetterExchange() != null && !request.deadLetterExchange().isBlank()) {
            arguments.put("x-dead-letter-exchange", request.deadLetterExchange());
        }
        if (request.messageTtl() != null) {
            arguments.put("x-message-ttl", request.messageTtl());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("durable", request.durable());
        body.put("arguments", arguments);

        put("/api/queues/" + VHOST_ENCODED + "/" + request.name(), body);
    }

    @Override
    public void deleteQueue(String name) {
        rejectIfProtected(name);
        try {
            delete("/api/queues/" + VHOST_ENCODED + "/" + name);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe la cola '" + name + "'");
        }
    }

    private boolean queueExists(String name) {
        try {
            get("/api/queues/" + VHOST_ENCODED + "/" + name);
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    // ------------------------------------------------------------- exchanges

    @Override
    public Object listExchanges() {
        return get("/api/exchanges/" + VHOST_ENCODED);
    }

    @Override
    public void createExchange(createExchangeRequest request) {
        rejectIfAmqPrefixed(request.name());

        if (exchangeExists(request.name())) {
            throw new ResourceAlreadyExistsException("Ya existe un exchange llamado '" + request.name() + "'");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("type", request.type().toRabbitType());
        body.put("durable", request.durable());

        put("/api/exchanges/" + VHOST_ENCODED + "/" + request.name(), body);
    }

    @Override
    public void deleteExchange(String name) {
        rejectIfProtected(name);
        try {
            delete("/api/exchanges/" + VHOST_ENCODED + "/" + name);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe el exchange '" + name + "'");
        }
    }

    private boolean exchangeExists(String name) {
        try {
            get("/api/exchanges/" + VHOST_ENCODED + "/" + name);
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    // -------------------------------------------------------------- bindings

    @Override
    public Object listBindings() {
        return get("/api/bindings/" + VHOST_ENCODED);
    }

    @Override
    public void createBinding(createBindingRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("routing_key", request.routingKey());

        String path = "/api/bindings/" + VHOST_ENCODED + "/e/" + request.exchange() + "/q/" + request.queue();
        try {
            post(path, body);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException(
                    "El exchange '" + request.exchange() + "' o la cola '" + request.queue() + "' no existen");
        }
    }

    @Override
    public void deleteBinding(String exchange, String queue, String routingKey) {
        if (properties.isProtected(exchange) || properties.isProtected(queue)) {
            throw new ProtectedResourceException(
                    "El binding entre '" + exchange + "' y '" + queue + "' pertenece al contrato de topología y no se puede eliminar");
        }


        URI uri = UriComponentsBuilder.fromUriString(managementUrl)
                .path("/api/bindings/{vhost}/e/{exchange}/q/{queue}/{routingKey}")
                .encode()
                .buildAndExpand("/", exchange, queue, routingKey)
                .toUri();

        try {
            restTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException(
                    "No existe un binding entre '" + exchange + "' y '" + queue + "' con routing key '" + routingKey + "'");
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new RabbitManagementUnavailableException("RabbitMQ no respondió al eliminar el binding", ex);
        }
    }

    // -------------------------------------------------------------- overview

    @Override
    public Object getOverview() {
        return get("/api/overview");
    }

    // --------------------------------------------------------------- helpers

    private void rejectIfAmqPrefixed(String name) {
        if (name != null && name.toLowerCase().startsWith(AMQ_RESERVED_PREFIX)) {
            throw new ProtectedResourceException(
                    "El prefijo 'amq.' está reservado por RabbitMQ y no se puede usar para nombrar recursos nuevos");
        }
    }

    private void rejectIfProtected(String name) {
        if (properties.isProtected(name)) {
            throw new ProtectedResourceException(
                    "'" + name + "' pertenece al contrato de topología y no se puede eliminar");
        }
    }

    private Object get(String path) {
        try {
            return restTemplate.getForObject(managementUrl + path, Object.class);
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new RabbitManagementUnavailableException("RabbitMQ no respondió a la solicitud", ex);
        }
    }

    private void put(String path, Object body) {
        try {
            restTemplate.put(managementUrl + path, body);
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new RabbitManagementUnavailableException("RabbitMQ no respondió a la solicitud", ex);
        }
    }

    private void post(String path, Object body) {
        try {
            restTemplate.postForLocation(managementUrl + path, body);
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new RabbitManagementUnavailableException("RabbitMQ no respondió a la solicitud", ex);
        }
    }

    private void delete(String path) {
        try {
            restTemplate.delete(managementUrl + path);
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new RabbitManagementUnavailableException("RabbitMQ no respondió a la solicitud", ex);
        }
    }
}

