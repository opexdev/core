package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.accountant.ports.postgres.dao.UserTradeVolumeRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class UserVolumePersisterImpl(private val repository: UserTradeVolumeRepository) : UserVolumePersister {

    override suspend fun update(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        valueUSDT: BigDecimal,
        valueIRT: BigDecimal
    ) {
        repository.insertOrUpdate(userId, currency, date, volume, valueUSDT, valueIRT).awaitSingleOrNull()
    }
}