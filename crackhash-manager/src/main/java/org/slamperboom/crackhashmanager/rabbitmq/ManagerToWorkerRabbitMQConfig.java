package org.slamperboom.crackhashmanager.rabbitmq;

import org.slamperboom.crackhashmanager.DTOs.CrackTaskDTO;
import org.slamperboom.crackhashmanager.tasks.TaskManager;
import org.slamperboom.crackhashmanager.tasks.TaskStatus;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerToWorkerRabbitMQConfig {
    public static final String managerToWorkerQueueName = "mtwQueue";
    public static final String managerToWorkerExchangeName = "mtwExchange";
    public static final String managerToWorkerRoutingKey = "mtwQueue";
    public static final String managerToWorkerTemplateName = "mtwTemplate";

    public static final String managerToWorkerDeadQueueName = "mtwQueue_dlq";
    private static final String managerToWorkerDeadRoutingKey = "mtwQueue_dlq";

    @Bean(name = managerToWorkerQueueName)
    Queue managerToWorkerQueue(){
        return QueueBuilder
                .durable(managerToWorkerQueueName)
                .ttl(5000)
                .deadLetterExchange(managerToWorkerExchangeName)
                .deadLetterRoutingKey(managerToWorkerDeadRoutingKey)
                .maxLength(1L)
                .overflow(QueueBuilder.Overflow.rejectPublish)
                .build();
    }

    @Bean(name = managerToWorkerExchangeName)
    DirectExchange managerToWorkerExchange(){
        return new DirectExchange(managerToWorkerExchangeName);
    }

    @Bean(name = managerToWorkerDeadQueueName)
    Queue managerToWorkerDeadLetterQueue(){
        return QueueBuilder
                .durable(managerToWorkerDeadQueueName)
                .build();
    }

    @Bean
    Binding mtwBinding(@Qualifier(managerToWorkerQueueName) Queue queue,
                       @Qualifier(managerToWorkerExchangeName) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(managerToWorkerRoutingKey);
    }

    @Bean
    Binding mtwDeadBinding(@Qualifier(managerToWorkerDeadQueueName) Queue queue,
                       @Qualifier(managerToWorkerExchangeName) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(managerToWorkerDeadRoutingKey);
    }

    @Bean(name = "managerConnectionFactory")
    public ConnectionFactory managerConnectionFactory(){
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setUsername("user");
        factory.setPassword("password");
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setPublisherReturns(true);
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = managerToWorkerTemplateName)
    public RabbitTemplate rabbitTemplate(@Qualifier("managerConnectionFactory") ConnectionFactory connectionFactory, TaskManager taskManager) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(managerToWorkerExchangeName);
        rabbitTemplate.setRoutingKey(managerToWorkerRoutingKey);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            Message rejectedMessage = correlationData.getReturned().getMessage();
            CrackTaskDTO crackTaskDTO =
                    (CrackTaskDTO) jsonMessageConverter().fromMessage(rejectedMessage);
            if(ack){
                taskManager.updateStatus(crackTaskDTO.taskId(), TaskStatus.IN_PROGRESS);
            }else{
                taskManager.updateStatus(crackTaskDTO.taskId(), TaskStatus.WAITING);
            }
        });
        return rabbitTemplate;
    }
}
