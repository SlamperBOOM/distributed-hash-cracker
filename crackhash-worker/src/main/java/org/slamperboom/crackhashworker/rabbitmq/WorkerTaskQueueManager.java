package org.slamperboom.crackhashworker.rabbitmq;

import org.slamperboom.crackhashworker.DTOs.CrackWorkerResultDTO;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.slamperboom.crackhashworker.rabbitmq.WorkerToManagerRabbitMQConfig.*;

@Component
public class WorkerTaskQueueManager {
    private final AmqpTemplate rabbitMQTemplate;

    public WorkerTaskQueueManager(@Qualifier(workerToManagerTemplateName)AmqpTemplate rabbitMQTemplate) {
        this.rabbitMQTemplate = rabbitMQTemplate;
    }

    public void uploadFinishedTask(CrackWorkerResultDTO crackWorkerResult){
        try {
            rabbitMQTemplate.convertAndSend(crackWorkerResult);
        }catch (AmqpException e){
        }
    }

    public void sendHealthCheck(long taskId){
        try {
            rabbitMQTemplate.convertAndSend(new CrackWorkerResultDTO(taskId, null, true));
        }catch (AmqpException e){
        }
    }
}
