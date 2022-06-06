package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.Transfer
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import co.nilin.opex.bcgateway.core.spi.WalletProxy
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WalletSyncServiceImpl(
    private val walletProxy: WalletProxy,
    private val assignedAddressHandler: AssignedAddressHandler,
    private val currencyHandler: CurrencyHandler
) : WalletSyncService {

    private val logger: Logger by LoggerDelegate()

    override suspend fun syncTransfers(transfers: List<Transfer>): Unit = coroutineScope {
        transfers.map { transfer ->
            async {
                assignedAddressHandler.findUuid(transfer.receiver.address, transfer.receiver.memo)?.also { uuid ->
                    logger.info("Deposit came for $uuid - to ${transfer.receiver.address}")
                    currencyHandler.findByChainAndTokenAddress(transfer.chain, transfer.tokenAddress)?.also { symbol ->
                        sendDeposit(uuid, symbol, transfer)
                    }
                }
            }
        }.awaitAll()
    }

    private suspend fun sendDeposit(uuid: String, currencyImpl: CurrencyImplementation, transfer: Transfer) {
        val amount = transfer.amount.divide(BigDecimal.TEN.pow(currencyImpl.decimal))
        val symbol = currencyImpl.currency.symbol
        logger.info("Sending deposit to $uuid - $amount $symbol")
        walletProxy.transfer(uuid, symbol, amount, transfer.txHash)
    }
}
