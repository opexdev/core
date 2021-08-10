package co.nilin.opex.app.spi

import co.nilin.opex.app.inout.PairFeeConfig
import co.nilin.opex.matching.core.model.OrderDirection

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}