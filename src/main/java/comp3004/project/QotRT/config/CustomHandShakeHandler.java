package comp3004.project.QotRT.config;

import comp3004.project.QotRT.model.Player;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

class CustomHandshakeHandler extends DefaultHandshakeHandler {
    // Custom class for storing principal
    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // Generate principal with UUID as name
        return new Player(UUID.randomUUID().toString());
    }
}
