package co.nilin.opex.websocket.core.spi

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.websocket.core.dto.EventSubscription
import co.nilin.opex.websocket.core.dto.EventType
import java.security.Principal

abstract class EventStreamHandler {

    protected val subscriptions = arrayListOf<EventSubscription>()

    fun addSubscription(path: String, sessionId: String, symbol: String, user: Principal?) {
        when (EventType.findByPath(path)) {
            EventType.Order -> subscriptions.add(EventSubscription(sessionId, EventType.Order, symbol, user))
            EventType.Trade -> subscriptions.add(EventSubscription(sessionId, EventType.Trade, symbol, user))
            null -> {
            }
        }
    }

    fun removeSubscription(sessionId: String) {
        val sub = subscriptions.find { it.sessionId == sessionId }
        if (sub != null)
            subscriptions.remove(sub)
    }

    fun hasSubscription(): Boolean {
        return subscriptions.isNotEmpty()
    }

    fun hasSubscriptionForEvent(event: EventType): Boolean {
        for (s in subscriptions)
            if (s.eventType == event)
                return true
        return false
    }

    fun hasSubscriptionForEventAndSymbol(event: EventType, symbol: String): Boolean {
        for (s in subscriptions)
            if (s.eventType == event && s.symbol == symbol)
                return true
        return false
    }

    abstract suspend fun handleOrder(order: RichOrder)

    abstract suspend fun handleTrade(trade: RichTrade)

}