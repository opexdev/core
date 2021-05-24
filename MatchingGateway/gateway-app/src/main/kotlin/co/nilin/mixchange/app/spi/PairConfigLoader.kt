package co.nilin.mixchange.app.spi

import co.nilin.mixchange.app.inout.PairFeeConfig
import co.nilin.mixchange.matching.core.model.OrderDirection

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}