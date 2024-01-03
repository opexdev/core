package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.model.PairConfigModel
import co.nilin.opex.accountant.ports.postgres.model.PairFeeConfigModel
import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.OrderDirection
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class PairConfigLoaderImpl(
    private val pairConfigRepository: PairConfigRepository,
    private val pairFeeConfigRepository: PairFeeConfigRepository
) : PairConfigLoader {

    override suspend fun loadPairConfigs(): List<PairConfig> {
        return pairConfigRepository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.asPairConfig() }
    }

    override suspend fun loadPairFeeConfigs(): List<PairFeeConfig> {
        return pairFeeConfigRepository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                val pairConfig = pairConfigRepository.findById(it.pairConfigId).awaitSingle().asPairConfig()
                PairFeeConfig(pairConfig, it.direction, it.userLevel, it.makerFee, it.takerFee)
            }
    }

    override suspend fun loadPairFeeConfigs(direction: OrderDirection, userLevel: String): List<PairFeeConfig> {
        return pairFeeConfigRepository.findByDirectionAndUserLevel(direction, userLevel)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                val pairConfig = pairConfigRepository.findById(it.pairConfigId).awaitSingle().asPairConfig()
                PairFeeConfig(
                    pairConfig,
                    it.direction,
                    it.userLevel,
                    it.makerFee,
                    it.takerFee
                )
            }
    }

    override suspend fun loadPairFeeConfigs(
        pair: String,
        direction: OrderDirection,
        userLevel: String
    ): PairFeeConfig? {
        val fee = pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(pair, direction, userLevel)
            .awaitSingleOrNull() ?: return null
        val pairConfig = pairConfigRepository.findById(fee.pairConfigId).awaitSingle().asPairConfig()
        return PairFeeConfig(
            pairConfig,
            fee.direction,
            fee.userLevel,
            fee.makerFee,
            fee.takerFee
        )
    }

    override suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig {
        val pairConfig = pairConfigRepository.findById(pair).awaitFirstOrElse {
            throw OpexError.InvalidPair.messageFormattedException(pair)
        }

        var pairFeeConfig: PairFeeConfigModel?
        if (userLevel.isEmpty()) {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                .awaitFirstOrElse {
                    throw OpexError.InvalidPair.messageFormattedException(pair)
                }
        } else {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, userLevel)
                .awaitFirstOrNull()
            if (pairFeeConfig == null) {
                pairFeeConfig = pairFeeConfigRepository
                    .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                    .awaitFirstOrElse {
                        throw OpexError.InvalidPair.messageFormattedException(pair)
                    }
            }
        }

        return PairFeeConfig(
            PairConfig(
                pair,
                pairConfig.leftSideWalletSymbol,
                pairConfig.rightSideWalletSymbol,
                pairConfig.leftSideFraction,
                pairConfig.rightSideFraction
            ),
            pairFeeConfig!!.direction,
            pairFeeConfig.userLevel,
            pairFeeConfig.makerFee,
            pairFeeConfig.takerFee
        )
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