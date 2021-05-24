package co.nilin.mixchange.app.spi

import co.nilin.mixchange.app.inout.PairFeeConfig
import co.nilin.mixchange.matching.core.model.OrderDirection
import java.math.BigDecimal


interface AccountantApiProxy {
    suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal): Boolean
    suspend fun fetchPairFeeConfig(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}