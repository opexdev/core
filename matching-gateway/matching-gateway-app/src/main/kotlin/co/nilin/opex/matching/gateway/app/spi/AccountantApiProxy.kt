package co.nilin.opex.matching.gateway.app.spi

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairFeeConfig
import java.math.BigDecimal


interface AccountantApiProxy {
    suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal): Boolean
    suspend fun fetchPairFeeConfig(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}