package co.nilin.opex.port.websocket.service.stream

import java.security.Principal

class PathPool<T>(val path: String, val pathType: T, val data: Array<Any>) {

    private val subscriptions = hashSetOf<Subscription>()

    fun addSubscription(sessionId: String, user: Principal? = null) {
        subscriptions.add(Subscription(sessionId, user))
    }

    fun removeSubscription(sessionId: String) {
        val sub = subscriptions.find { it.sessionId == sessionId }
        if (sub != null)
            subscriptions.remove(sub)
    }

    fun hasAnySubscriber() = subscriptions.isNotEmpty()

    fun numberOfSubscribers() = subscriptions.size
}