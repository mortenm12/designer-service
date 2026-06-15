package dk.tinker.designer.config;

import dk.tinker.designer.event.TokenEventConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange tokenEventsExchange() {
        return new TopicExchange(TokenEventConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue tokenInvalidationQueue() {
        return new Queue(TokenEventConstants.QUEUE_TOKEN_INVALIDATION, true);
    }

    @Bean
    public Binding apiKeyRevokedBinding(Queue tokenInvalidationQueue,
            TopicExchange tokenEventsExchange) {
        return BindingBuilder.bind(tokenInvalidationQueue)
                .to(tokenEventsExchange)
                .with(TokenEventConstants.ROUTING_KEY_API_KEY_REVOKED);
    }

    @Bean
    public Binding sessionLogoutBinding(Queue tokenInvalidationQueue,
            TopicExchange tokenEventsExchange) {
        return BindingBuilder.bind(tokenInvalidationQueue)
                .to(tokenEventsExchange)
                .with(TokenEventConstants.ROUTING_KEY_SESSION_LOGOUT);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
