package co.nilin.opex.websocket.app.socket

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer

@Configuration
class WebSocketAuthorizationConfig : AbstractSecurityWebSocketMessageBrokerConfigurer() {

    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
        with(messages) {
            simpDestMatchers("/secured/**").hasAuthority("SCOPE_trust")
            anyMessage().permitAll()
        }
    }

    override fun sameOriginDisabled(): Boolean {
        return true
    }

}