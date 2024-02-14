package co.nilin.opex.market.ports.postgres

import co.nilin.opex.market.ports.postgres.util.CacheWrapper
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MarketCacheHandler(cacheManager: CacheManager) {

    private val cacheWrapper = CacheWrapper(cacheManager, "marketQuery")

    private fun getTradeTickerDataCache(startFrom: LocalDateTime) {

    }
}