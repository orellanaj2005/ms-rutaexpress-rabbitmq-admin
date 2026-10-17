package cl.rutaexpress.rabbitmqadmin.controller;

import cl.rutaexpress.rabbitmqadmin.dto.CreateBindingRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateExchangeRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateQueueRequest;
import cl.rutaexpress.rabbitmqadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ---------------------------------------------------------------- queues

    @GetMapping("/queues")
    public ResponseEntity<Object> listQueues() {
        return ResponseEntity.ok(service.listQueues());
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<Object> getQueue(@PathVariable String name) {
        return ResponseEntity.ok(service.getQueue(name));
    }

    @PostMapping("/queues")
    public ResponseEntity<Void> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        service.createQueue(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------- exchanges

    @GetMapping("/exchanges")
    public ResponseEntity<Object> listExchanges() {
        return ResponseEntity.ok(service.listExchanges());
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        service.createExchange(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- bindings

    @GetMapping("/bindings")
    public ResponseEntity<Object> listBindings() {
        return ResponseEntity.ok(service.listBindings());
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody CreateBindingRequest request) {
        service.createBinding(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(
            @RequestParam String exchange,
            @RequestParam String queue,
            @RequestParam String routingKey) {
        service.deleteBinding(exchange, queue, routingKey);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- overview

    @GetMapping("/overview")
    public ResponseEntity<Object> getOverview() {
        return ResponseEntity.ok(service.getOverview());
    }
}