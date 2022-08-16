package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.Transfer
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import co.nilin.opex.bcgateway.ports.postgres.impl.ChainHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.slf4j.Logger
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WalletSyncController(private val chainHandler: ChainHandler, private val walletSyncService: WalletSyncService) {
    private val logger: Logger by LoggerDelegate()

    @PutMapping("wallet-sync/{chain}")
    suspend fun syncTransferOnChain(@PathVariable chain: String, @RequestBody transfers: List<Transfer>) {
        logger.debug("Received ${transfers.size} transfer(s) for chain: $chain")
        runCatching {
            chainHandler.fetchChainInfo(chain)
        }.onFailure {
            throw OpexException(OpexError.NotFound)
        }.onSuccess {
            walletSyncService.syncTransfers(transfers)
        }
    }
}
