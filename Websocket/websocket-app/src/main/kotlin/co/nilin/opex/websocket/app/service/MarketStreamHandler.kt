package co.nilin.opex.websocket.app.service

import co.nilin.opex.port.websocket.app.dto.Interval
import co.nilin.opex.port.websocket.app.service.stream.IntervalStreamHandler
import co.nilin.opex.port.websocket.app.service.stream.StreamJob
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MarketStreamHandler(
    private val marketService: MarketService,
    template: SimpMessagingTemplate,
    userRegistry: SimpUserRegistry
) : IntervalStreamHandler<MarketDestinationType>(template, userRegistry) {

    override fun getPath(type: MarketDestinationType) = type.path

    override fun createJob(type: MarketDestinationType) = when (type) {
        is MarketDestinationType.Depth -> StreamJob(2, TimeUnit.SECONDS) {
            marketService.getOrderBookDepth(type.symbol)
        }
        is MarketDestinationType.Price -> StreamJob(2, TimeUnit.SECONDS) {
            marketService.getPriceChange()
        }
        is MarketDestinationType.Candle -> {
            val i = Interval.findByLabel(type.interval)
            StreamJob(i?.duration ?: 2, i?.unit ?: TimeUnit.SECONDS) {
                marketService.getCandleData(type.symbol, type.interval)
            }
        }
        is MarketDestinationType.Overview -> StreamJob(2, TimeUnit.SECONDS) {
            marketService.getPriceOverview(type.symbol, type.duration)
        }
        is MarketDestinationType.RecentTrades -> StreamJob(2, TimeUnit.SECONDS) {
            marketService.getRecentTrades(type.symbol)
        }
    }

}