package co.nilin.opex.wallet.app.service

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.TransferRequest
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.TransferManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class TransferService(
    private val transferManager: TransferManager,
    private val currencyService: CurrencyService,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager
) {

    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    suspend fun transfer(
        symbol: String,
        senderWalletType: String,
        senderUuid: String,
        receiverWalletType: String,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: String = "NO_CATEGORY",
        additionalData: Map<String, Any>? = emptyMap()
    ): TransferResult {
        if (senderWalletType == "cashout" || receiverWalletType == "cashout")
            throw OpexException(OpexError.InvalidCashOutUsage)
        val currency = currencyService.getCurrency(symbol) ?: throw OpexException(OpexError.CurrencyNotFound)
        val sourceOwner = walletOwnerManager.findWalletOwner(senderUuid)
            ?: throw OpexException(OpexError.WalletOwnerNotFound)
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, senderWalletType, currency)
            ?: throw OpexException(OpexError.WalletNotFound)

        val receiverOwner = walletOwnerManager.findWalletOwner(receiverUuid) ?: walletOwnerManager.createWalletOwner(
            senderUuid,
            "not set",
            ""
        )
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            receiverOwner, receiverWalletType, currency
        ) ?: walletManager.createWallet(
            receiverOwner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            receiverWalletType
        )

        logger.info(
            "Transferring funds: $amount ${sourceWallet.owner.id}-${sourceWallet.currency.symbol}-$senderWalletType " +
                    "==> ${receiverWallet.owner.id}-${receiverWallet.currency.symbol}-$receiverWalletType "
        )

        return transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, amount),
                description, transferRef, transferCategory, additionalData
            )
        ).transferResult
    }

    @Transactional
    suspend fun batchTransfer(request: List<TransferRequest>) {
        request.filter { it.receiverWalletType != "cashout" && it.senderWalletType != "cashout" }
            .forEach {
                val currency = currencyService.getCurrency(it.symbol)
                    ?: throw OpexException(OpexError.CurrencyNotFound)
                val sourceOwner = walletOwnerManager.findWalletOwner(it.senderUuid)
                    ?: throw OpexException(OpexError.WalletOwnerNotFound)
                val sourceWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, it.senderWalletType, currency)
                        ?: throw OpexException(OpexError.WalletNotFound)

                val receiverOwner = walletOwnerManager.findWalletOwner(it.receiverUuid)
                    ?: walletOwnerManager.createWalletOwner(it.senderUuid, "not set", "")

                val receiverWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(receiverOwner, it.receiverWalletType, currency)
                        ?: walletManager.createWallet(
                            receiverOwner,
                            Amount(currency, BigDecimal.ZERO),
                            currency,
                            it.receiverWalletType
                        )
                transferManager.transfer(
                    TransferCommand(
                        sourceWallet,
                        receiverWallet,
                        Amount(sourceWallet.currency, it.amount),
                        it.description,
                        it.transferRef,
                        it.transferCategory,
                        it.additionalData
                    )
                )
            }
    }

    suspend fun deposit(
        symbol: String,
        receiverUuid: String,
        receiverWalletType: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?
    ): TransferResult {
        if (receiverWalletType == "cashout") throw OpexException(OpexError.InvalidCashOutUsage)
        val systemUuid = "1"
        val currency = currencyService.getCurrency(symbol) ?: throw OpexException(OpexError.CurrencyNotFound)
        val sourceOwner = walletOwnerManager.findWalletOwner(systemUuid)
            ?: throw OpexException(OpexError.WalletOwnerNotFound)
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, "main", currency)
            ?: throw OpexException(OpexError.WalletNotFound)

        val receiverOwner = walletOwnerManager.findWalletOwner(receiverUuid) ?: walletOwnerManager.createWalletOwner(
            systemUuid,
            "not set",
            ""
        )
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            receiverOwner, receiverWalletType, currency
        ) ?: walletManager.createWallet(
            receiverOwner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            receiverWalletType
        )
        return transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, amount),
                description, transferRef, "DEPOSIT", emptyMap()
            )
        ).transferResult
    }

}