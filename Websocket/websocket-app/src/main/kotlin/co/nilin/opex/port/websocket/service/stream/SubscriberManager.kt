package co.nilin.opex.port.websocket.service.stream

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.web.socket.messaging.*

@Configuration
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class SubscriberManager {

    private val handlers = hashSetOf<StreamHandler<*>>()

    fun register(handler: StreamHandler<*>) {
        handlers.add(handler)
    }

    private fun removeSubscription(path: String, sessionId: String) {
        handlers.forEach {
            it.removeSubscription(path, sessionId)
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
        val headers = event.message.headers
        removeSubscription(headers["simpDestination"] as String, headers["simpSessionId"] as String)
    }

    @Bean
    fun sessionUnsubscribeEventListener() = ApplicationListener<SessionUnsubscribeEvent> { event ->
        println("- unsubscribed: ${event.message}")
    }

}