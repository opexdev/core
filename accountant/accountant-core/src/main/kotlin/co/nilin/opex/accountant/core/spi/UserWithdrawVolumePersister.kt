package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.UserTotalVolumeValue
import java.math.BigDecimal
import java.time.LocalDate

interface UserWithdrawVolumePersister {
    suspend fun update(
        userId: String,
        currency: String,
        amount: BigDecimal,
        date: LocalDate,
    )

    suspend fun getUserVolumeData(uuid: String, startDate: LocalDate): UserTotalVolumeValue?
}