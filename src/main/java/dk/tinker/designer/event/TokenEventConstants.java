package dk.tinker.designer.event;

public final class TokenEventConstants {

    public static final String EXCHANGE = "auth.events";
    public static final String ROUTING_KEY_API_KEY_REVOKED = "token.api-key.revoked";
    public static final String ROUTING_KEY_SESSION_LOGOUT = "token.session.logout";
    public static final String QUEUE_TOKEN_INVALIDATION = "designer.token-invalidation";

    private TokenEventConstants() { }
}
