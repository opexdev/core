package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import java.time.LocalDate

interface TotalAssetsSnapshotManager {
    suspend fun createSnapshot()
    suspend fun getUserLastSnapshot(
        uuid: String
    ): TotalAssetsSnapshot?
    suspend fun getLastDaysBalance(
        userId: String,
        startDate: LocalDate?,
        quatCurrency: String?,
        lastDays: Long = 31
    ): List<DailyAmount>


}