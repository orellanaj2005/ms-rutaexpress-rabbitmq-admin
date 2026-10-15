package cl.rutaexpress.rabbitmqadmin.controller;

import cl.rutaexpress.rabbitmqadmin.config.SecurityConfig;
import cl.rutaexpress.rabbitmqadmin.exception.ProtectedResourceException;
import cl.rutaexpress.rabbitmqadmin.exception.ResourceNotFoundException;
import cl.rutaexpress.rabbitmqadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@WebMvcTest(RabbitAdminController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "TENANT_ID=00000000-0000-0000-0000-000000000000",
        "API_CLIENT_ID=00000000-0000-0000-0000-000000000000"
})
class RabbitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitAdminService service;

    
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("Admin");
    private static final SimpleGrantedAuthority DESPACHADOR = new SimpleGrantedAuthority("Despachador");

    @Test
    void listQueuesReturns200ForAdmin() throws Exception {
        when(service.listQueues()).thenReturn(List.of(Map.of("name", "q.cmd.email")));

        mockMvc.perform(get("/api/rabbitmq/queues").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void listQueuesReturns403ForNonAdminRole() throws Exception {
        mockMvc.perform(get("/api/rabbitmq/queues").with(jwt().authorities(DESPACHADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listQueuesReturns401WithoutAToken() throws Exception {
        mockMvc.perform(get("/api/rabbitmq/queues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getQueueReturns404WhenTheServiceReportsItMissing() throws Exception {
        when(service.getQueue("no-existe")).thenThrow(new ResourceNotFoundException("No existe la cola 'no-existe'"));

        mockMvc.perform(get("/api/rabbitmq/queues/no-existe").with(jwt().authorities(ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void createQueueReturns201OnSuccess() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .with(jwt().authorities(ADMIN))
                        .contentType("application/json")
                        .content("{\"name\":\"q.nueva\",\"durable\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createQueueReturns400ForAnEmptyName() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .with(jwt().authorities(ADMIN))
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"durable\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void createExchangeReturns400ForAnInvalidType() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/exchanges")
                        .with(jwt().authorities(ADMIN))
                        .contentType("application/json")
                        .content("{\"name\":\"x.valido\",\"type\":\"NO_EXISTE\",\"durable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteQueueReturns409WhenTheServiceReportsItIsProtected() throws Exception {
        doThrow(new ProtectedResourceException("'q.cmd.email' pertenece al contrato de topología y no se puede eliminar"))
                .when(service).deleteQueue("q.cmd.email");

        mockMvc.perform(delete("/api/rabbitmq/queues/q.cmd.email").with(jwt().authorities(ADMIN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PROTECTED_RESOURCE"));
    }

    @Test
    void deleteQueueReturns204OnSuccess() throws Exception {
        mockMvc.perform(delete("/api/rabbitmq/queues/q.de.prueba").with(jwt().authorities(ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBindingReadsTheThreeQueryParams() throws Exception {
        mockMvc.perform(delete("/api/rabbitmq/bindings")
                        .param("exchange", "cmd.topic")
                        .param("queue", "q.cmd.warehouse")
                        .param("routingKey", "warehouse.#")
                        .with(jwt().authorities(ADMIN)))
                .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(service).deleteBinding(eq("cmd.topic"), eq("q.cmd.warehouse"), eq("warehouse.#"));
    }

    @Test
    void overviewReturns200ForAdmin() throws Exception {
        when(service.getOverview()).thenReturn(Map.of("rabbitmq_version", "3.13.0"));

        mockMvc.perform(get("/api/rabbitmq/overview").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk());
    }
}
