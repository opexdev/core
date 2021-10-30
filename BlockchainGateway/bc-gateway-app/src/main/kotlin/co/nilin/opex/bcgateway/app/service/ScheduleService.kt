package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.core.api.ChainSyncService
import co.nilin.opex.bcgateway.core.api.WalletSyncService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val chainService: ChainSyncService,
    private val walletService: WalletSyncService
) {

    @Scheduled(fixedDelay = 1000)
    fun start() {
        runBlocking {
            chainService.startSyncWithChain()
            walletService.startSyncWithWallet()
        }
    }

}