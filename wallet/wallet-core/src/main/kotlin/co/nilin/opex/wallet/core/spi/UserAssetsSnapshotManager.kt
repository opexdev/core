package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.core.model.UserDetailAssetsSnapshot
import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import java.time.LocalDate

interface UserAssetsSnapshotManager {
    suspend fun createTotalAssetsSnapshot()
    suspend fun createDetailAssetsSnapshot()
    suspend fun getUserLastTotalAssetsSnapshot(
        uuid: String
    ): TotalAssetsSnapshot?

    suspend fun getUsersLastDetailAssetsSnapshot(
        limit: Int,
        offset: Int,
    ): List<UserDetailAssetsSnapshot>

    suspend fun getLastDaysBalance(
        userId: String,
        startDate: LocalDate?,
        quatCurrency: String?,
        lastDays: Long = 31
    ): List<DailyAmount>


}