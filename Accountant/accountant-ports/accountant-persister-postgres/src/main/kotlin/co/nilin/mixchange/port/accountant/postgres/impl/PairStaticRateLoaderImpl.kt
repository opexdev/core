package co.nilin.mixchange.port.accountant.postgres.impl

import co.nilin.mixchange.accountant.core.spi.PairStaticRateLoader
import co.nilin.mixchange.port.accountant.postgres.dao.PairConfigRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class PairStaticRateLoaderImpl(val pairConfigRepository: PairConfigRepository): PairStaticRateLoader {
    override suspend fun calculateStaticRate(leftSide: String, rightSide: String): Double? {
        val pairConfig = pairConfigRepository
            .findById("${leftSide}_$rightSide")
            .awaitFirstOrElse { throw IllegalArgumentException("${leftSide}_$rightSide is not available") }
        return pairConfig.rate
    }
}