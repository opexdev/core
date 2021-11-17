package co.nilin.opex.port.websocket.service.stream

import java.security.Principal

class PathPool<T>(val path: String, val pathType: T) {

    private val subscriptions = arrayListOf<Subscription>()

    fun addSub(sessionId: String, user: Principal? = null) {
        subscriptions.add(Subscription(sessionId, user))
    }

    fun removeSub(sessionId: String) {
        val sub = subscriptions.find { it.sessionId == sessionId }
        if (sub != null)
            subscriptions.remove(sub)
    }

    fun hasAnySubscriber() = subscriptions.isNotEmpty()

    fun numberOfSubscribers() = subscriptions.size
}