package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.model.PairConfigModel
import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.OrderDirection
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component

@Component
class PairConfigLoaderImpl(
    private val pairConfigRepository: PairConfigRepository,
) : PairConfigLoader {

    override suspend fun loadPairConfigs(): List<PairConfig> {
        return pairConfigRepository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.asPairConfig() }
    }

    override suspend fun load(pair: String, direction: OrderDirection): PairConfig {
        return pairConfigRepository
            .findById(pair).awaitFirstOrElse {
                throw OpexError.InvalidPair.messageFormattedException(pair)
            }.let {
                PairConfig(
                    it.pair,
                    it.leftSideWalletSymbol,
                    it.rightSideWalletSymbol,
                    it.leftSideFraction,
                    it.rightSideFraction
                )
            }
    }

    private fun PairConfigModel.asPairConfig() = PairConfig(
        pair,
        leftSideWalletSymbol,
        rightSideWalletSymbol,
        leftSideFraction,
        rightSideFraction
    )
}