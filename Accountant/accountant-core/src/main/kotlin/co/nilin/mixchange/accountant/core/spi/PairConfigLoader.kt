package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.model.PairFeeConfig
import co.nilin.mixchange.matching.core.model.OrderDirection

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String):PairFeeConfig
}