package co.nilin.opex.port.accountant.postgres.impl

import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.PairConfigLoader
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.port.accountant.postgres.dao.PairConfigRepository
import co.nilin.opex.port.accountant.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.port.accountant.postgres.model.PairFeeConfigModel
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class PairConfigLoaderImpl(
    val pairConfigRepository: PairConfigRepository, val pairFeeConfigRepository: PairFeeConfigRepository
) : PairConfigLoader {

    override suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig {
        val pairConfig = pairConfigRepository
            .findById(pair).awaitFirstOrElse {
                val error = OpexError.InvalidPair
                throw OpexException(error, String.format(error.message!!, pair))
            }
        var pairFeeConfig: PairFeeConfigModel?
        if (userLevel.isEmpty()) {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                .awaitFirstOrElse {
                    val error = OpexError.InvalidPair
                    throw OpexException(error, String.format(error.message!!, pair))
                }
        } else {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, userLevel)
                .awaitFirstOrNull()
            if (pairFeeConfig == null) {
                pairFeeConfig = pairFeeConfigRepository
                    .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                    .awaitFirstOrElse {
                        val error = OpexError.InvalidPairFee
                        throw OpexException(error, String.format(error.message!!, pair))
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
            ), pairFeeConfig!!.direction, pairFeeConfig.userLevel, pairFeeConfig.makerFee, pairFeeConfig.takerFee
        )

    }
}