package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.inout.ManualGatewayCommand
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.inout.TransferResultDetailed
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.*
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


@Service
class TransferService(
    private val transferManager: TransferManager,
    private val currencyManager: CurrencyServiceManager,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val currencyGraph: GraphService,
    private val reservedTransferManager: ReservedTransferManager,
    private val meterRegistry: MeterRegistry,
    private val currencyService: CurrencyServiceV2

) {

    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    suspend fun transfer(
        symbol: String,
        senderWalletType: WalletType,
        senderUuid: String,
        receiverWalletType: WalletType,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: TransferCategory = TransferCategory.NO_CATEGORY
    ): TransferResultDetailed {
        return _transfer(
            symbol,
            senderWalletType,
            senderUuid,
            receiverWalletType,
            receiverUuid,
            amount,
            description,
            transferRef,
            transferCategory
        )
    }

    suspend fun calculateDestinationAmount(
        symbol: String,
        amount: BigDecimal,
        destSymbol: String,
    ): BigDecimal {
        val rate = currencyGraph.buildRoutes(symbol, destSymbol)
            .map { route -> Rate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate()) }
            .firstOrNull() ?: throw OpexError.NOT_EXCHANGEABLE_CURRENCIES.exception()
        return amount.multiply(rate.rate)
    }
    suspend fun calculateSourceAmount(
            symbol: String,
            amount: BigDecimal,
            destSymbol: String,
    ): BigDecimal {
        val rate = currencyGraph.buildRoutes(symbol, destSymbol)
                .map { route -> Rate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate()) }
                .firstOrNull() ?: throw OpexError.NOT_EXCHANGEABLE_CURRENCIES.exception()
        return amount.divide(rate.rate,10,RoundingMode.HALF_UP)
    }

    suspend fun reserveTransfer(
        sourceAmount: BigDecimal,
        sourceSymbol: String,
        destSymbol: String,
        senderUuid: String,
        senderWalletType: WalletType,
        receiverUuid: String,
        receiverWalletType: WalletType
    ): ReservedTransferResponse {
        val rate = currencyGraph.buildRoutes(sourceSymbol, destSymbol)
            .map { route -> Rate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate()) }
            .firstOrNull() ?: throw OpexError.NOT_EXCHANGEABLE_CURRENCIES.exception()
        val finalAmount = sourceAmount.multiply(rate.rate)

        if (sourceAmount == BigDecimal.ZERO || finalAmount == BigDecimal.ZERO)
            throw OpexError.InvalidAmount.exception()

        checkIfSystemHasEnoughBalance(destSymbol, receiverWalletType, finalAmount)
        val reserveNumber = UUID.randomUUID().toString()
        val resp = reservedTransferManager.reserve(
            ReservedTransfer(
                reserveNumber = reserveNumber,
                destSymbol = destSymbol,
                sourceSymbol = sourceSymbol,
                sourceAmount = sourceAmount,
                senderUuid = senderUuid,
                receiverUuid = receiverUuid,
                senderWalletType = senderWalletType,
                receiverWalletType = receiverWalletType,
                reservedDestAmount = finalAmount,
                rate = rate.rate
            )
        )

        return with(resp) {
            ReservedTransferResponse(
                reserveNumber,
                sourceSymbol,
                destSymbol,
                receiverUuid,
                sourceAmount,
                reservedDestAmount,
                reserveDate,
                expDate,
                status
            )
        }
    }

    @Transactional
    suspend fun advanceTransfer(
        reserveNumber: String,
        description: String?,
        transferRef: String?,
        issuer: String? = null,
        //todo need to review
        transferCategory: TransferCategory = TransferCategory.PURCHASE_FINALIZED
    ): TransferResultDetailed {
        val reservations = reservedTransferManager.fetchValidReserve(reserveNumber)
            ?: throw OpexError.InvalidReserveNumber.exception()
        if (!(issuer == null || reservations.senderUuid == issuer))
            throw OpexError.Forbidden.exception()

        val senderTransfer = _transfer(
            reservations.sourceSymbol,
            reservations.senderWalletType,
            reservations.senderUuid,
            WalletType.MAIN,
            walletOwnerManager.systemUuid,
            reservations.sourceAmount,
            description,
            "$transferRef-$reserveNumber-withdraw",
            transferCategory,
            reservations.sourceSymbol,
            reservations.sourceAmount
        ).transferResult

        val receiverTransfer = _transfer(
            reservations.destSymbol,
            WalletType.MAIN,
            walletOwnerManager.systemUuid,
            reservations.receiverWalletType,
            reservations.receiverUuid,
            reservations.reservedDestAmount,
            description,
            "$transferRef-$reserveNumber-deposit",
            transferCategory,
            reservations.destSymbol,
            reservations.reservedDestAmount
        ).transferResult

        reservedTransferManager.commitReserve(reserveNumber)
        return TransferResultDetailed(
            transferResult = TransferResult(
                senderTransfer.date,
                senderTransfer.sourceUuid,
                senderTransfer.sourceWalletType,
                senderTransfer.sourceBalanceBeforeAction,
                senderTransfer.sourceBalanceAfterAction,
                senderTransfer.amount,
                receiverTransfer.destUuid,
                receiverTransfer.destWalletType,
                receiverTransfer.receivedAmount
            ), ""
        )
    }

    private suspend fun _transfer(
        symbol: String,
        senderWalletType: WalletType,
        senderUuid: String,
        receiverWalletType: WalletType,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: TransferCategory = TransferCategory.NO_CATEGORY,
        destSymbol: String = symbol,
        destAmount: BigDecimal = amount
    ): TransferResultDetailed {
        if (senderWalletType == WalletType.CASHOUT || receiverWalletType == WalletType.CASHOUT)
            throw OpexError.InvalidCashOutUsage.exception()
        val sourceCurrency = currencyManager.fetchCurrency(FetchCurrency(symbol = destSymbol))
            ?: throw OpexError.CurrencyNotFound.exception()
        val sourceOwner = walletOwnerManager.findWalletOwner(senderUuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, senderWalletType, sourceCurrency)
                ?: throw OpexError.WalletNotFound.exception()

//todo what should we do for admin receiver
        val receiverOwner = walletOwnerManager.findWalletOwner(receiverUuid) ?: walletOwnerManager.createWalletOwner(
            receiverUuid,
            "not set",
            "1"
        )
        val receiverCurrency = currencyManager.fetchCurrency(FetchCurrency(symbol = destSymbol))
            ?: throw OpexError.CurrencyNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            receiverOwner, receiverWalletType, receiverCurrency
        ) ?: walletManager.createWallet(
            receiverOwner,
            Amount(receiverCurrency, BigDecimal.ZERO),
            receiverCurrency,
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
                description,
                transferRef,
                transferCategory,
                Amount(receiverWallet.currency, destAmount)
            )
        )
    }


    private suspend fun checkIfSystemHasEnoughBalance(
        destSymbol: String,
        receiverWalletType: WalletType,
        finalAmount: BigDecimal?
    ) {
        val destCurrency = currencyManager.fetchCurrency(FetchCurrency(symbol = destSymbol))!!
        val system = walletOwnerManager.findWalletOwner(walletOwnerManager.systemUuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()

        val systemWallet = walletManager.findWalletByOwnerAndCurrencyAndType(system, receiverWalletType, destCurrency)
            ?: throw OpexError.WalletNotFound.exception()

        if (systemWallet.balance.amount < finalAmount) {
            throw OpexError.CurrentSystemAssetsAreNotEnough.exception()
        }
    }

    internal suspend fun isManualDepositAllowed(symbol: String, gatewayUuid: String): Boolean {
        var gateway = currencyService.fetchCurrencyGateway(gatewayUuid, symbol)
        return gateway is ManualGatewayCommand && gateway.depositAllowed == true && gateway.isActive == true
    }


}