package org.slamperboom.crackhashworker.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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

    private static final String managerToWorkerDeadQueueName = "mtwQueue_dlq";
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer>
    listenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(1);
        return factory;
    }
}
