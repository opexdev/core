package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.WithdrawStatus
import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.accountant.ports.postgres.dao.UserWithdrawVolumeRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset


@Component
class UserWithdrawVolumePersisterImpl(
    private val repository: UserWithdrawVolumeRepository,
    private val currencyRatePersister: CurrencyRatePersister,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
    @Value("\${app.withdraw-volume-calculation-currency}") private val calculationCurrency: String
) : UserWithdrawVolumePersister {

    override suspend fun update(
        userId: String,
        currency: String,
        amount: BigDecimal,
        date: LocalDateTime,
        withdrawStatus: WithdrawStatus
    ) {
        val rate = if (currency == calculationCurrency) BigDecimal.ONE
        else currencyRatePersister.getRate(currency, calculationCurrency)

        val signedAmount = amount.multiply(rate)
            .let { if (withdrawStatus == WithdrawStatus.CANCELED || withdrawStatus == WithdrawStatus.REJECTED) it.negate() else it }

        repository.insertOrUpdate(
            userId,
            date.atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate(),
            signedAmount,
            calculationCurrency
        ).awaitSingleOrNull()
    }

    override suspend fun getTotalValueByUserAndDateAfter(
        uuid: String,
        startDate: LocalDateTime
    ): BigDecimal {
        return repository.findTotalValueByUserAndAndDateAfter(
            uuid,
            startDate.atOffset(ZoneOffset.of(zoneOffsetString)).toLocalDate(),
            calculationCurrency
        ).awaitFirstOrNull() ?: BigDecimal.ZERO
    }
}