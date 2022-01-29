package co.nilin.opex.matching.gateway.app.spi

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.inout.PairFeeConfig

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}