package org.slamperboom.crackhashworker.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerToManagerRabbitMQConfig {
    public static final String workerToManagerQueueName = "wtmQueue";
    public static final String workerToManagerExchangeName = "wtmExchange";
    public static final String workerToManagerRoutingKey = "wtmQueue";
    public static final String workerToManagerTemplateName = "wtmTemplate";

    @Bean(name = workerToManagerQueueName)
    Queue workerToManagerQueue(){
        return new Queue(workerToManagerQueueName, true);
    }

    @Bean(name = workerToManagerExchangeName)
    DirectExchange workerToManagerExchange(){
        return new DirectExchange(workerToManagerExchangeName);
    }

    @Bean
    Binding wtmBinding(@Qualifier(workerToManagerQueueName) Queue queue,
                       @Qualifier(workerToManagerExchangeName) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(workerToManagerRoutingKey);
    }

    @Bean(name = workerToManagerTemplateName)
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRoutingKey(workerToManagerRoutingKey);
        rabbitTemplate.setExchange(workerToManagerExchangeName);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
