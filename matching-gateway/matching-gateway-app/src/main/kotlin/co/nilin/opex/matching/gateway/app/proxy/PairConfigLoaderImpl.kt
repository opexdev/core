package co.nilin.opex.matching.gateway.app.proxy

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.postgres.util.CacheManager
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class PairConfigLoaderImpl(
    private val accountantApiProxy: AccountantApiProxy,
    private val cacheManager: CacheManager<String, PairConfig>
) : PairConfigLoader {
    override suspend fun load(pair: String, direction: OrderDirection): PairConfig {
        return cacheManager.get("pair-config:$pair-$direction")
            ?: accountantApiProxy.fetchPairConfig(pair, direction)
                .also {
                    cacheManager.put(
                        "pair-config:$pair-$direction",
                        it,
                        5, TimeUnit.MINUTES
                    )

                }
    }
}