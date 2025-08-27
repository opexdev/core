package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.UserTotalVolumeValue
import java.math.BigDecimal
import java.time.LocalDate

interface UserVolumePersister {

    suspend fun update(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        valueUSDT: BigDecimal,
        valueIRT: BigDecimal
    )

    suspend fun getUserVolumeData(uuid: String, startDate: LocalDate): UserTotalVolumeValue?
}