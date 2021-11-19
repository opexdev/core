package co.nilin.opex.websocket.core.dto

import java.security.Principal
import java.util.*

data class EventSubscription(
    val sessionId: String,
    val eventType: EventType,
    val symbol: String,
    val user: Principal? = null,
    val time: Long = Date().time
)

enum class EventType(val path: String) {
    Order("/order"),
    Trade("/trade");

    companion object {
        fun findByPath(path: String): EventType? {
            return values().find { it.path == path }
        }
    }
}