package co.nilin.opex.websocket.app.socket

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.web.socket.messaging.*

@Configuration
class StompEventsConfig {

    private val logger = LoggerFactory.getLogger(StompEventsConfig::class.java)

    @Bean
    fun brokerAvailabilityListener() = ApplicationListener<BrokerAvailabilityEvent> { event ->
        logger.info("Is broker available: ${event.isBrokerAvailable}")
    }

    @Bean
    fun sessionConnectListener() = ApplicationListener<SessionConnectEvent> { event ->
        logger.info("* session connect received: ${event.message}")
    }

    @Bean
    fun sessionConnectedListener() = ApplicationListener<SessionConnectedEvent> { event ->
        logger.info("* connected: ${event.message}")
    }

    @Bean
    fun sessionDisconnectedListener() = ApplicationListener<SessionDisconnectEvent> { event ->
        logger.info("* disconnected: ${event.message}")
    }

    @Bean
    fun sessionSubscribeListener() = ApplicationListener<SessionSubscribeEvent> { event ->
        logger.info("+ subscribed: ${event.message}")
    }

    @Bean
    fun sessionUnsubscribeEventListener() = ApplicationListener<SessionUnsubscribeEvent> { event ->
        logger.info("- unsubscribed: ${event.message}")
    }

}