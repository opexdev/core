package co.nilin.opex.matching.gateway.app.proxy

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.postgres.util.RedisCacheHelper
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class PairConfigLoaderImpl(
    private val accountantApiProxy: AccountantApiProxy,
    private val redisCacheHelper: RedisCacheHelper
) : PairConfigLoader {
    override suspend fun load(pair: String, direction: OrderDirection): PairConfig {
        return redisCacheHelper.get<PairConfig>("pair-config:$pair-$direction")
            ?: accountantApiProxy.fetchPairConfig(pair, direction)
                .also {
                    redisCacheHelper.put(
                        "pair-config:$pair-$direction",
                        it,
                        DynamicInterval(5, TimeUnit.MINUTES)
                    )

                }
    }
}