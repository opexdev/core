package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.PairRateConfig

interface PriceConfigPersister {
    suspend fun savePairRateConfig(config: PairRateConfig): PairRateConfig
    suspend fun deletePairRateConfig(symbol: String)
}
