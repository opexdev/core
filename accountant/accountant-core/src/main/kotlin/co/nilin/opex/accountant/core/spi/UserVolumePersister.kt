package co.nilin.opex.accountant.core.spi

import java.math.BigDecimal
import java.time.LocalDate

interface UserVolumePersister {

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
}