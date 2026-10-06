package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.PairRateConfig
import co.nilin.opex.price.core.spi.PriceConfigPersister
import co.nilin.opex.price.ports.postgres.dao.PairRateConfigRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PriceConfigPersisterImpl(
    private val pairRateConfigRepository: PairRateConfigRepository
) : PriceConfigPersister {

    override suspend fun savePairRateConfig(config: PairRateConfig): PairRateConfig {
        require(config.margin == null || config.margin in BigDecimal.ZERO..BigDecimal.ONE) {
            "margin must be between 0 and 1, got ${config.margin} for symbol=${config.symbol}"
        }

        pairRateConfigRepository.upsert(
            symbol = config.symbol,
            strategy = config.strategy?.name,
            margin = config.margin,
            isActive = config.isActive,
            priceMode = config.priceMode.name
        ).awaitFirstOrNull()
        return config
    }

    override suspend fun deletePairRateConfig(symbol: String) {
        pairRateConfigRepository.findBySymbol(symbol)
            .awaitFirstOrNull()
            ?.let { pairRateConfigRepository.delete(it).awaitFirstOrNull() }
    }
}
