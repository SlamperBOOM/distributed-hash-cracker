package org.slamperboom.crackhashworker.rabbitmq;

import org.slamperboom.crackhashworker.DTOs.CrackTaskDTO;
import org.slamperboom.crackhashworker.worker.Worker;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.slamperboom.crackhashworker.rabbitmq.ManagerToWorkerRabbitMQConfig.managerToWorkerQueueName;

@Component
public class WorkerTaskQueueListener {
    private final Worker worker;

    public WorkerTaskQueueListener(Worker worker) {
        this.worker = worker;
    }

    @RabbitListener(queues = managerToWorkerQueueName, containerFactory = "listenerContainerFactory")
    public void listenForTasks(CrackTaskDTO crackTaskDTO) {
        worker.workerThread(crackTaskDTO);
    }
}
