package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.spi.TotalAssetsSnapshotManager
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class WalletSnapshotService(private val totalAssetsSnapshotManager: TotalAssetsSnapshotManager) {

    @Scheduled(cron = "0 59 14 * * ?") //FIXME TEST
    fun createSnapshots() {
        runBlocking { totalAssetsSnapshotManager.createSnapshotForAllOwners() }
    }

}