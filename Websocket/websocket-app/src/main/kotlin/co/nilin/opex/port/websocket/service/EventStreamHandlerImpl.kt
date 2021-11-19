package co.nilin.opex.port.websocket.service

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.port.websocket.config.AppDispatchers
import co.nilin.opex.websocket.core.dto.EventType
import co.nilin.opex.websocket.core.spi.EventStreamHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class EventStreamHandlerImpl(private val template: SimpMessagingTemplate) : EventStreamHandler() {

    override suspend fun handleOrder(order: RichOrder) {

    }

    override suspend fun handleTrade(trade: RichTrade) {

    }

    suspend fun send(path: String, data: Any, eventType: EventType) {
        withContext(AppDispatchers.websocketExecutor) {
            template.convertAndSend(path, data)
        }
    }

    suspend fun sendToUser(path: String, data: Any, uuid: String, eventType: EventType) {
        withContext(AppDispatchers.websocketExecutor) {
            template.convertAndSendToUser(uuid, path, data)
        }
    }

}