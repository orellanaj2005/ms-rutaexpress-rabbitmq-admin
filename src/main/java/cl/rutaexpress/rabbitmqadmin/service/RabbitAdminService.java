package cl.rutaexpress.rabbitmqadmin.service;

import cl.rutaexpress.rabbitmqadmin.dto.createBindingRequest;
import cl.rutaexpress.rabbitmqadmin.dto.createExchangeRequest;
import cl.rutaexpress.rabbitmqadmin.dto.createQueueRequest;


public interface RabbitAdminService {

    Object listQueues();

    Object getQueue(String name);

    void createQueue(createQueueRequest request);

    void deleteQueue(String name);

    Object listExchanges();

    void createExchange(createExchangeRequest request);

    void deleteExchange(String name);

    Object listBindings();

    void createBinding(createBindingRequest request);

    void deleteBinding(String exchange, String queue, String routingKey);

    Object getOverview();
}
