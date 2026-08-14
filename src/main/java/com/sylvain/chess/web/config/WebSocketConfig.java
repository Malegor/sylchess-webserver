package com.sylvain.chess.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Allows React to connect to your backend
    registry.addEndpoint("/chess-socket")
            .setAllowedOrigins("http://localhost:5173") // Vite React Default URL
            .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic"); // For outbound messages (Server -> React)
    registry.setApplicationDestinationPrefixes("/app"); // For inbound messages (React -> Server)
  }
}