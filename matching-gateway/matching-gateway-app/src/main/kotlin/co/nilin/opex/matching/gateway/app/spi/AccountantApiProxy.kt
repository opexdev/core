package co.nilin.opex.matching.gateway.app.spi

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import java.math.BigDecimal


interface AccountantApiProxy {
    suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal?): Boolean
    suspend fun fetchPairConfig(pair: String, direction: OrderDirection): PairConfig
}