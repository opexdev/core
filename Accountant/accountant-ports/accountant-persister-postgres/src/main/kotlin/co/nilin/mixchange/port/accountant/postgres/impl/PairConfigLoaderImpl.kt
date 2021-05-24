package co.nilin.mixchange.port.accountant.postgres.impl

import co.nilin.mixchange.accountant.core.model.PairConfig
import co.nilin.mixchange.accountant.core.model.PairFeeConfig
import co.nilin.mixchange.accountant.core.spi.PairConfigLoader
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.port.accountant.postgres.dao.PairConfigRepository
import co.nilin.mixchange.port.accountant.postgres.dao.PairFeeConfigRepository
import co.nilin.mixchange.port.accountant.postgres.model.PairFeeConfigModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.lang.IllegalArgumentException

@Component
class PairConfigLoaderImpl(
    val pairConfigRepository: PairConfigRepository, val pairFeeConfigRepository: PairFeeConfigRepository
) : PairConfigLoader {
    override suspend fun load(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig {
        val pairConfig = pairConfigRepository
            .findById(pair).awaitFirstOrElse { throw IllegalArgumentException("$pair is not available") }
        var pairFeeConfig: PairFeeConfigModel?
        if ( userLevel.isEmpty()) {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                .awaitFirstOrElse { throw IllegalArgumentException("$pair fee is not available") }
        } else {
            pairFeeConfig = pairFeeConfigRepository
                .findByPairAndDirectionAndUserLevel(pair, direction, userLevel)
                .awaitFirstOrNull()
            if ( pairFeeConfig == null ){
                pairFeeConfig = pairFeeConfigRepository
                    .findByPairAndDirectionAndUserLevel(pair, direction, "*")
                    .awaitFirstOrElse { throw IllegalArgumentException("$pair fee is not available") }
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