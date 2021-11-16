package co.nilin.opex.port.websocket.service

import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_SINGLETON)
class SubscriberPool(private val template: SimpMessagingTemplate) {

    private val public = hashSetOf<String>()
    private val secured = hashSetOf<String>()

    fun sendPublicMessage(path: String, message: Any) {
        if (hasPublicSubscriber())
            template.convertAndSend(path, message)
    }

    fun sendSecuredMessage(path: String, user: String, message: Any) {
        if (hasSecuredSubscriber())
            template.convertAndSendToUser(user, path, message)
    }

    fun addPublicSubscriber(sub: String) {
        public.add(sub)
    }

    fun addSecuredSubscriber(sub: String) {
        secured.add(sub)
    }

    fun hasPublicSubscriber() = public.isNotEmpty()

    fun hasSecuredSubscriber() = secured.isNotEmpty()

    fun hasAnySubscriber() = hasPublicSubscriber() && hasSecuredSubscriber()

}