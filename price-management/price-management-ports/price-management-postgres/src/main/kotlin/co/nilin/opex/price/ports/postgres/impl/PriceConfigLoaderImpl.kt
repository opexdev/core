package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.PairRateConfig
import co.nilin.opex.price.core.spi.PriceConfigLoader
import co.nilin.opex.price.ports.postgres.dao.PairRateConfigRepository
import co.nilin.opex.price.ports.postgres.utils.asCoreModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class PriceConfigLoaderImpl(
    private val pairRateConfigRepository: PairRateConfigRepository
) : PriceConfigLoader {

    override suspend fun loadPairRateConfigs(): List<PairRateConfig> {
        return pairRateConfigRepository.findAll()
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }

    override suspend fun loadPairRateConfig(symbol: String): PairRateConfig? {
        return pairRateConfigRepository.findBySymbol(symbol)
            .awaitFirstOrNull()
            ?.asCoreModel()
    }

    override suspend fun loadActiveAutoConfigs(): List<PairRateConfig> {
        return pairRateConfigRepository.findActiveAutoConfigs()
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }
}
