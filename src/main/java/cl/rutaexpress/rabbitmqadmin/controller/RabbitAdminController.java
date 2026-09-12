package cl.rutaexpress.rabbitmqadmin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitAdminController {

    private final RestTemplate restTemplate;

    @Value("${rabbitmq.management-url}")
    private String managementUrl;

    public RabbitAdminController(@Qualifier("rabbitRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/queues")
    public ResponseEntity<Object> listQueues() {
        return restTemplate.getForEntity(managementUrl + "/api/queues", Object.class);
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<Object> getQueue(@PathVariable String name) {
        return restTemplate.getForEntity(managementUrl + "/api/queues/%2F/" + name, Object.class);
    }

    @GetMapping("/overview")
    public ResponseEntity<Object> getOverview() {
        return restTemplate.getForEntity(managementUrl + "/api/overview", Object.class);
    }
}