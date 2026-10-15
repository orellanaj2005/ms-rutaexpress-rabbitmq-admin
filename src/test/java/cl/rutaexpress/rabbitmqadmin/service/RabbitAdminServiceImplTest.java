package cl.rutaexpress.rabbitmqadmin.service;

import cl.rutaexpress.rabbitmqadmin.config.RabbitAdminProperties;
import cl.rutaexpress.rabbitmqadmin.dto.CreateBindingRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateExchangeRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateQueueRequest;
import cl.rutaexpress.rabbitmqadmin.dto.ExchangeType;
import cl.rutaexpress.rabbitmqadmin.exception.ProtectedResourceException;
import cl.rutaexpress.rabbitmqadmin.exception.RabbitManagementUnavailableException;
import cl.rutaexpress.rabbitmqadmin.exception.ResourceAlreadyExistsException;
import cl.rutaexpress.rabbitmqadmin.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RabbitAdminServiceImplTest {

    private static final String MANAGEMENT_URL = "http://rabbit-test:15672";

    private MockRestServiceServer server;
    private RabbitAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        RabbitAdminProperties properties = new RabbitAdminProperties();
        properties.setProtected(List.of("q.cmd.email", "cmd.direct", "q.audit.deadletter"));

        service = new RabbitAdminServiceImpl(restTemplate, properties, MANAGEMENT_URL);
    }

    @Test
    void listQueuesReturnsTheRawManagementApiResponse() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/queues"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"name\":\"q.cmd.email\"}]", MediaType.APPLICATION_JSON));

        Object result = service.listQueues();

        assertInstanceOf(List.class, result);
        server.verify();
    }

    @Test
    void getQueueTranslates404IntoResourceNotFoundException() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/queues/%2F/no-existe"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> service.getQueue("no-existe"));
        server.verify();
    }

    @Test
    void createQueueRejectsAmqPrefixedNamesWithoutCallingRabbit() {
        CreateQueueRequest request = new CreateQueueRequest("amq.algo", true, null, null);

        assertThrows(ProtectedResourceException.class, () -> service.createQueue(request));
        server.verify();
    }

    @Test
    void createQueueFailsWithConflictWhenTheQueueAlreadyExists() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/queues/%2F/q.ya.existe"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"name\":\"q.ya.existe\"}", MediaType.APPLICATION_JSON));

        CreateQueueRequest request = new CreateQueueRequest("q.ya.existe", true, null, null);

        assertThrows(ResourceAlreadyExistsException.class, () -> service.createQueue(request));
        server.verify();
    }

    @Test
    void createQueueDeclaresItWithTheGivenArguments() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/queues/%2F/q.nueva"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo(MANAGEMENT_URL + "/api/queues/%2F/q.nueva"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        CreateQueueRequest request = new CreateQueueRequest("q.nueva", true, "cmd.dead.dlx", 60000L);

        service.createQueue(request);
        server.verify();
    }

    @Test
    void deleteQueueBlocksResourcesFromTheProtectedList() {
        assertThrows(ProtectedResourceException.class, () -> service.deleteQueue("q.cmd.email"));
    }

    @Test
    void deleteExchangeBlocksResourcesFromTheProtectedList() {
        assertThrows(ProtectedResourceException.class, () -> service.deleteExchange("cmd.direct"));
    }

    @Test
    void createExchangeSendsTheLowercaseTypeToRabbit() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/exchanges/%2F/x.nuevo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo(MANAGEMENT_URL + "/api/exchanges/%2F/x.nuevo"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        CreateExchangeRequest request = new CreateExchangeRequest("x.nuevo", ExchangeType.TOPIC, true);

        service.createExchange(request);
        server.verify();
    }

    @Test
    void createBindingTranslates404IntoResourceNotFoundException() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/bindings/%2F/e/no-existe/q/q.cmd.email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CreateBindingRequest request = new CreateBindingRequest("no-existe", "q.cmd.email", "email.send");

        assertThrows(ResourceNotFoundException.class, () -> service.createBinding(request));
        server.verify();
    }

    @Test
    void deleteBindingBlocksBindingsThatTouchAProtectedResource() {
        assertThrows(ProtectedResourceException.class, () -> service.deleteBinding("cmd.direct", "q.algo", "algo"));
    }

    @Test
    void deleteBindingEncodesTopicWildcardsInTheRoutingKey() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/bindings/%2F/e/cmd.topic/q/q.cmd.warehouse/warehouse.%23"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        service.deleteBinding("cmd.topic", "q.cmd.warehouse", "warehouse.#");
        server.verify();
    }

    @Test
    void anyRabbitServerErrorSurfacesAsManagementUnavailable() {
        server.expect(requestTo(MANAGEMENT_URL + "/api/overview"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(RabbitManagementUnavailableException.class, () -> service.getOverview());
    }
}

