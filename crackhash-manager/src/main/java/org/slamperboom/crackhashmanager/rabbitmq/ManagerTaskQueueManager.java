package org.slamperboom.crackhashmanager.rabbitmq;

import org.slamperboom.crackhashmanager.DTOs.CrackTaskDTO;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.slamperboom.crackhashmanager.rabbitmq.ManagerToWorkerRabbitMQConfig.*;

@Component
public class ManagerTaskQueueManager {
    private final RabbitTemplate mtwTemplate;
    private final MessageConverter converter;

    public ManagerTaskQueueManager(@Qualifier(managerToWorkerTemplateName) RabbitTemplate mtwTemplate, MessageConverter converter) {
        this.mtwTemplate = mtwTemplate;
        this.converter = converter;
    }

    public boolean uploadTask(CrackTaskDTO crackTask){
        try {
            var correlationData = new CorrelationData();
            correlationData.setReturned(new ReturnedMessage(converter.toMessage(crackTask, new MessageProperties(), CrackTaskDTO.class), 0, "", managerToWorkerExchangeName, managerToWorkerRoutingKey));
            mtwTemplate.convertAndSend(crackTask, message -> message, correlationData);
            return true;
        }catch (AmqpException e){
            return false;
        }
    }
}
