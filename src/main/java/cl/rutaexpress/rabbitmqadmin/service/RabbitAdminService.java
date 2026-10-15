package cl.rutaexpress.rabbitmqadmin.service;

import cl.rutaexpress.rabbitmqadmin.dto.CreateBindingRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateExchangeRequest;
import cl.rutaexpress.rabbitmqadmin.dto.CreateQueueRequest;


public interface RabbitAdminService {

    Object listQueues();

    Object getQueue(String name);

    void createQueue(CreateQueueRequest request);

    void deleteQueue(String name);

    Object listExchanges();

    void createExchange(CreateExchangeRequest request);

    void deleteExchange(String name);

    Object listBindings();

    void createBinding(CreateBindingRequest request);

    void deleteBinding(String exchange, String queue, String routingKey);

    Object getOverview();
}
