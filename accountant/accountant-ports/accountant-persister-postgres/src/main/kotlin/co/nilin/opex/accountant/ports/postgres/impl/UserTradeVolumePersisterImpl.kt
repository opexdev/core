package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.DailyAmount
import co.nilin.opex.accountant.core.spi.UserTradeVolumePersister
import co.nilin.opex.accountant.ports.postgres.dao.UserTradeVolumeRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class UserTradeVolumePersisterImpl(
    private val repository: UserTradeVolumeRepository,
    @Value("\${app.zone-offset}") private val zoneOffsetString: String,
    @Value("\${app.trade-volume-calculation-currency}") private val calculationCurrency: String,

    ) : UserTradeVolumePersister {

    override suspend fun update(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        totalAmount: BigDecimal,
        quoteCurrency: String
    ) {
        repository.insertOrUpdate(userId, currency, date, volume, totalAmount, quoteCurrency).awaitSingleOrNull()
    }

    override suspend fun getUserTotalTradeVolume(
        uuid: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): BigDecimal? {
        return repository.findTotalValueByUserAndAndDateAfter(uuid, startDate, quoteCurrency).awaitSingleOrNull()
    }

    override suspend fun getUserTotalTradeVolumeByCurrency(
        uuid: String,
        currency: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): BigDecimal? {
        return repository.findTotalValueByUserAndAndDateAfterAndCurrency(uuid, currency, startDate, quoteCurrency)
            .awaitSingleOrNull()
    }

    override suspend fun getLastDaysTrade(
        userId: String,
        startDate: LocalDate?,
        quatCurrency: String?,
        lastDays: Long
    ): List<DailyAmount> {

        val startDate = startDate ?: LocalDate
            .now(ZoneOffset.of(zoneOffsetString))
            .minusDays(lastDays)

        return repository
            .findDailyTradeVolume(userId, startDate, quatCurrency ?: calculationCurrency)
            .map {
                DailyAmount(
                    date = it.date,
                    totalAmount = it.totalAmount
                )
            }
            .collectList()
            .awaitSingle()
    }
}