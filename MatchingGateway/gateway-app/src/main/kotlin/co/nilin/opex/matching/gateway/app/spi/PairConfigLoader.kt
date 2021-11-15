package co.nilin.opex.matching.gateway.app.spi

import co.nilin.opex.matching.gateway.app.inout.PairFeeConfig
import co.nilin.opex.matching.core.model.OrderDirection

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}