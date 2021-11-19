package co.nilin.opex.port.websocket.service

import co.nilin.opex.port.websocket.config.AppDispatchers
import co.nilin.opex.port.websocket.service.stream.MarketPathType
import co.nilin.opex.port.websocket.service.stream.StreamHandler
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class MarketStreamHandler(
    private val service: MarketService,
    private val template: SimpMessagingTemplate
) : StreamHandler<MarketPathType>("/market") {

    override fun isPathSubscribable(path: String): Boolean {
        return MarketPathType.isValidPath(path)
    }

    fun addOrderBookSub(symbol: String, sessionId: String) {
        //TODO validate path
        addSubscription("/depth/$symbol", MarketPathType.Depth, sessionId, arrayOf(symbol))
    }

    fun addCandleDataSub(symbol: String, sessionId: String) {
        addSubscription("/kline/$symbol", MarketPathType.Candle, sessionId, arrayOf(symbol))
    }

    fun priceChange(symbol: String, duration: String, sessionId: String) {
        addSubscription("/ticker/$symbol-$duration", MarketPathType.Ticker, sessionId, arrayOf(symbol, duration))
    }

    @Scheduled(fixedDelay = 2000)
    private fun interval() {
        for (it in map.entries) {
            if (!it.value.hasAnySubscriber())
                continue

            runBlocking(AppDispatchers.websocketExecutor) {
                when (it.value.pathType) {
                    MarketPathType.Depth -> orderBook(it.key, it.value.data[0] as String?)
                    MarketPathType.Candle -> TODO()
                    MarketPathType.Ticker -> TODO()
                }
            }
        }
    }

    private suspend fun orderBook(path: String, symbol: String?) {
        if (symbol == null)
            return

        val depth = service.getOrderBookDepth(symbol)
        template.convertAndSend(path, depth)
    }

}