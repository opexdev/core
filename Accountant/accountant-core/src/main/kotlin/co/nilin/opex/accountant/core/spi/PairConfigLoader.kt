package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.matching.core.model.OrderDirection

interface PairConfigLoader {
    suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig
}