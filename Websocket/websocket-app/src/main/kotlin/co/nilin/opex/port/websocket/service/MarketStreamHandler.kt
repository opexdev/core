package co.nilin.opex.port.websocket.service

import co.nilin.opex.port.websocket.service.stream.MarketPathType
import co.nilin.opex.port.websocket.service.stream.StreamHandler
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class MarketStreamHandler(val template: SimpMessagingTemplate) : StreamHandler<MarketPathType>() {

    fun addOrderBookSub(symbol: String, sessionId: String) {
        //TODO validate path
        addSubscription("/market/depth/$symbol", MarketPathType.Depth(symbol), sessionId)
    }

    fun addCandleDataSub(symbol: String, sessionId: String) {
        addSubscription("/market/kline/$symbol", MarketPathType.Candle(symbol), sessionId)
    }

    fun priceChange(symbol: String, duration: String, sessionId: String) {
        addSubscription("/market/ticker/$symbol-$duration", MarketPathType.Ticker(symbol, duration), sessionId)
    }

    override fun isPathSubscribable(path: String): Boolean {
        return false
    }

}