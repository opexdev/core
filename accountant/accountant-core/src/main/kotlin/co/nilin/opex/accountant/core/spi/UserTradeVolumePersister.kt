package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.DailyAmount
import java.math.BigDecimal
import java.time.LocalDate

interface UserTradeVolumePersister {

    suspend fun update(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        totalAmount: BigDecimal,
        quoteCurrency: String
    )

    suspend fun getUserTotalTradeVolume(uuid: String, startDate: LocalDate, quoteCurrency: String): BigDecimal?
    suspend fun getUserTotalTradeVolumeByCurrency(
        uuid: String,
        currency: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): BigDecimal?
    suspend fun getLastDaysTrade(userId: String, startDate: LocalDate?, quatCurrency: String?, lastDays: Long = 31): List<DailyAmount>

}