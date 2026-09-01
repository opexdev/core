package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.PairRateConfig

interface PriceConfigLoader {
    suspend fun loadPairRateConfigs(): List<PairRateConfig>
    suspend fun loadPairRateConfig(symbol: String): PairRateConfig?

    /** Configs the AUTO scheduler should process: active and in AUTO mode. */
    suspend fun loadActiveAutoConfigs(): List<PairRateConfig>
}
