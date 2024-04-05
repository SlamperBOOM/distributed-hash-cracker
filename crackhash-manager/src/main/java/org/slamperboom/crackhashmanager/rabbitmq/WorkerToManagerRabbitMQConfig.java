package org.slamperboom.crackhashmanager.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerToManagerRabbitMQConfig {
    public static final String workerToManagerQueueName = "wtmQueue";
    public static final String workerToManagerExchangeName = "wtmExchange";
    public static final String workerToManagerRoutingKey = "wtmQueue";

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
}
