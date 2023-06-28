package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.spi.WalletManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class BackupService(private val walletManager: WalletManager) {

    @Scheduled(initialDelay = 60000, fixedDelay = 1000 * 60 * 30)
    private fun backup() {

    }

}