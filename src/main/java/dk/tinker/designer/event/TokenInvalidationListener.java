package dk.tinker.designer.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class TokenInvalidationListener {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public TokenInvalidationListener(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = TokenEventConstants.QUEUE_TOKEN_INVALIDATION)
    public void handleTokenEvent(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        try {
            if (TokenEventConstants.ROUTING_KEY_API_KEY_REVOKED.equals(routingKey)) {
                ApiKeyRevokedEvent event = objectMapper.readValue(
                        message.getBody(), ApiKeyRevokedEvent.class);
                evictApiKey(event.keyHash());
            } else if (TokenEventConstants.ROUTING_KEY_SESSION_LOGOUT.equals(routingKey)) {
                clearAllApiKeys();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to process token invalidation event", ex);
        }
    }

    private void evictApiKey(String keyHash) {
        var cache = cacheManager.getCache("apiKeyValidation");
        if (cache != null) {
            cache.evict(keyHash);
        }
    }

    private void clearAllApiKeys() {
        var cache = cacheManager.getCache("apiKeyValidation");
        if (cache != null) {
            cache.clear();
        }
    }
}
