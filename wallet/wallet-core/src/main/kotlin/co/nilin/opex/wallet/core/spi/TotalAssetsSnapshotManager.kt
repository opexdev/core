package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot

interface TotalAssetsSnapshotManager {
    suspend fun createSnapshot()
    suspend fun getUserLastSnapshot(
        uuid: String
    ): TotalAssetsSnapshot?
}