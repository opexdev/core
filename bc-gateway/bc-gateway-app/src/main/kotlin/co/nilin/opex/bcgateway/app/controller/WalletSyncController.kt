package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.Transfer
import co.nilin.opex.bcgateway.ports.postgres.impl.ChainHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WalletSyncController(private val chainHandler: ChainHandler, private val walletSyncService: WalletSyncService) {
    @PutMapping("wallet-sync/{chain}")
    suspend fun syncTransferOnChain(@PathVariable chain: String, @RequestBody transfers: List<Transfer>) {
        runCatching { chainHandler.fetchChainInfo(chain) }.onFailure { throw OpexException(OpexError.NotFound) }
        walletSyncService.syncTransfers(transfers)
    }
}
