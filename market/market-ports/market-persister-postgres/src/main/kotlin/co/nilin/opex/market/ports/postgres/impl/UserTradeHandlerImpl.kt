package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.UserTradeVolumeRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class UserTradeHandlerImpl(private val repository: UserTradeVolumeRepository) {

    suspend fun getVolume(uuid: String, pair: String, date: LocalDate): BigDecimal {
        return repository.findByUserAndPairAndDateAfter(uuid, pair, date).awaitFirstOrNull()?.value ?: BigDecimal.ZERO
    }

    suspend fun calculateTotalVolume(uuid: String, date: LocalDate) {
        val userVolumes = repository.findAllByUserAndDateAfter(uuid, date)
            .collectList()
            .awaitFirstOrElse { emptyList() }

    }
}