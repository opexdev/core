package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.matching.engine.core.model.OrderDirection

interface PairConfigLoader {

    suspend fun loadPairConfigs(): List<PairConfig>

    suspend fun load(pair: String, direction: OrderDirection): PairConfig
}