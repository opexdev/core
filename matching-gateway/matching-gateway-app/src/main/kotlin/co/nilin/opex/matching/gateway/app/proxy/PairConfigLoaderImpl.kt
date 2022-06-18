package co.nilin.opex.matching.gateway.app.proxy

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import org.springframework.stereotype.Service

@Service
class PairConfigLoaderImpl(private val accountantApiProxy: AccountantApiProxy) : PairConfigLoader {
    override suspend fun load(pair: String, direction: OrderDirection): PairConfig {
        return accountantApiProxy.fetchPairConfig(pair, direction)
    }
}