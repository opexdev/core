package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.inout.UserTotalVolumeValue
import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class UserWithdrawVolumePersisterImpl(
    private val repository: UserWithdrawVolumeRepository,
    private val currencyRatePersister: CurrencyRatePersister
) : UserWithdrawVolumePersister {

    override suspend fun update(
        userId: String,
        currency: String,
        amount: BigDecimal,
        date: LocalDate,
    ) {
        val valueUSDT = amount * currencyRatePersister.getRate(currency, "USDT")
        val valueIRT = amount * currencyRatePersister.getRate(currency, "IRT")
        repository.insertOrUpdate(userId, date, valueUSDT, valueIRT).awaitSingleOrNull()
    }

    override suspend fun getUserVolumeData(uuid: String, startDate: LocalDate): UserTotalVolumeValue? {
        return repository.findTotalValueByUserAndAndDateAfter(uuid, startDate).awaitSingleOrNull()
    }
}