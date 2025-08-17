package co.nilin.opex.wallet.core.spi

interface TotalAssetsSnapshotManager {
    suspend fun createSnapshotForAllOwners()
}