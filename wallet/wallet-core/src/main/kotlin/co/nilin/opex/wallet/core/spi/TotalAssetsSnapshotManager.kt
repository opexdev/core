package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import java.time.LocalDateTime

interface TotalAssetsSnapshotManager {
    suspend fun createSnapshot()
    suspend fun getByOwnerIdAndDate(
        ownerId: Long,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null
    ): List<TotalAssetsSnapshot>
}