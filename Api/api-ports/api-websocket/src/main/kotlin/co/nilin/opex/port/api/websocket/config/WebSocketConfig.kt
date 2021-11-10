package co.nilin.opex.port.api.websocket.config

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.*

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/socket")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        with(registry) {
            enableSimpleBroker("/topic/", "/queue/")
            setApplicationDestinationPrefixes("/app")
        }
    }

    @Bean
    fun brokerAvailabilityListener() = ApplicationListener<BrokerAvailabilityEvent> { event ->
        println("Is broker available: ${event.isBrokerAvailable}")
    }

    @Bean
    fun sessionConnectListener() = ApplicationListener<SessionConnectEvent> { event ->
        println("* session connect received: ${event.message}")
    }

    @Bean
    fun sessionConnectedListener() = ApplicationListener<SessionConnectedEvent> { event ->
        println("* connected: ${event.message}")
    }

    @Bean
    fun sessionDisconnectedListener() = ApplicationListener<SessionDisconnectEvent> { event ->
        println("* disconnected: ${event.message}")
    }

    @Bean
    fun sessionSubscribeListener() = ApplicationListener<SessionSubscribeEvent> { event ->
        println("+ subscribed: ${event.message}")
    }

    @Bean
    fun sessionUnsubscribeEventListener() = ApplicationListener<SessionUnsubscribeEvent> { event ->
        println("- unsubscribed: ${event.message}")
    }

}