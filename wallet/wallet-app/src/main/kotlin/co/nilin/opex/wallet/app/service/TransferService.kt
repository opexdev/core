package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.inout.TransferResultDetailed
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.service.PrecisionService
import co.nilin.opex.wallet.core.spi.*
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
    private val precisionService: PrecisionService,

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
        transferCategory: TransferCategory = TransferCategory.NO_CATEGORY,
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
        return amount.divide(rate.rate, 10, RoundingMode.HALF_UP)
    }

    suspend fun reserveTransfer(
        sourceAmount: BigDecimal,
        sourceSymbol: String,
        destSymbol: String,
        senderUuid: String,
        senderWalletType: WalletType,
        receiverUuid: String,
        receiverWalletType: WalletType,
    ): ReservedTransferResponse {
        validateInitialAmountAndPrecision(sourceAmount, sourceSymbol)

        val rate = fetchRateOrThrow(sourceSymbol, destSymbol)
        val destAmount = calculateDestAmount(sourceAmount, rate)
        val scaledDestAmount = precisionService.calculatePrecision(destAmount, destSymbol)

        validateMinimumAmount(sourceSymbol, sourceAmount, scaledDestAmount, destSymbol, rate.rate)
        validateMaximumAmount(sourceSymbol, sourceAmount, destSymbol, scaledDestAmount)
        precisionService.validatePrecision(scaledDestAmount, destSymbol)

        checkIfSystemHasEnoughBalance(destSymbol, receiverWalletType, scaledDestAmount)

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
                reservedDestAmount = scaledDestAmount,
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

    suspend fun reserveTransferByAdmin(
        sourceSymbol: String,
        destSymbol: String,
        senderUuid: String,
        senderWalletType: WalletType,
        receiverUuid: String,
        receiverWalletType: WalletType,
        sourceAmount: BigDecimal,
        destAmount: BigDecimal? = null,
        rate: BigDecimal? = null,
    ): ReservedTransferResponse {

        if ((rate != null && destAmount != null) || (rate == null && destAmount == null)) {
            throw OpexError.BadRequest.exception("Either 'rate' or 'destAmount' must be provided, but not both")
        }

        if (rate != null && rate <= BigDecimal.ZERO) {
            throw OpexError.BadRequest.exception("rate must be greater than zero")
        }

        if (destAmount != null && destAmount <= BigDecimal.ZERO) {
            throw OpexError.BadRequest.exception("destAmount must be greater than zero")
        }

        val (finalRate, rawDestAmount) = if (rate != null) {
            val calculatedDest = calculateDestAmount(sourceAmount, Rate(sourceSymbol, destSymbol, rate))
            Pair(rate, calculatedDest)
        } else {
            val calculatedRate = destAmount!!.divide(sourceAmount, 28, RoundingMode.HALF_UP)
            Pair(calculatedRate, destAmount)
        }

        validateInitialAmountAndPrecision(sourceAmount, sourceSymbol)
        val scaledDestAmount = precisionService.calculatePrecision(rawDestAmount, destSymbol)

        validateMinimumAmount(sourceSymbol, sourceAmount, scaledDestAmount, destSymbol, finalRate)
        validateMaximumAmount(sourceSymbol, sourceAmount, destSymbol, scaledDestAmount)
        precisionService.validatePrecision(scaledDestAmount, destSymbol)
        checkIfSystemHasEnoughBalance(destSymbol, receiverWalletType, scaledDestAmount)
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
                reservedDestAmount = scaledDestAmount,
                rate = finalRate
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
        transferCategory: TransferCategory = TransferCategory.PURCHASE_FINALIZED,
    ): TransferResultDetailed {
        return executeAdvanceTransfer(
            reserveNumber = reserveNumber,
            description = description,
            transferRef = transferRef,
            transferCategory = transferCategory,
            issuer = issuer,
            validateIssuer = true
        )
    }

    @Transactional
    suspend fun advanceTransferByAdmin(
        reserveNumber: String,
        description: String?,
        transferRef: String?,
        transferCategory: TransferCategory = TransferCategory.IMPERSONATED_PURCHASE_FINALIZED,
    ): TransferResultDetailed {
        return executeAdvanceTransfer(
            reserveNumber = reserveNumber,
            description = description,
            transferRef = transferRef,
            transferCategory = transferCategory,
            issuer = null,
            validateIssuer = false
        )
    }

    private suspend fun executeAdvanceTransfer(
        reserveNumber: String,
        description: String?,
        transferRef: String?,
        transferCategory: TransferCategory,
        issuer: String?,
        validateIssuer: Boolean
    ): TransferResultDetailed {

        val reservations = reservedTransferManager.fetchValidReserve(reserveNumber)
            ?: throw OpexError.InvalidReserveNumber.exception()

        if (validateIssuer && !(issuer == null || reservations.senderUuid == issuer)) {
            throw OpexError.Forbidden.exception()
        }

        val refPrefix = transferRef?.let { "$it-" } ?: ""
        val withdrawRef = "${refPrefix}${reserveNumber}-withdraw"
        val depositRef = "${refPrefix}${reserveNumber}-deposit"

        val senderTransfer = (
            symbol = reservations.sourceSymbol,
            senderWalletType = reservations.senderWalletType,
            senderUuid = reservations.senderUuid,
            receiverWalletType = WalletType.MAIN,
            receiverUuid = walletOwnerManager.systemUuid,
            amount = reservations.sourceAmount,
            description = description,
            transferRef = withdrawRef,
            transferCategory = transferCategory,
            destSymbol = reservations.destSymbol,
            destAmount = reservations.sourceAmount
        ).transferResult

        val receiverTransfer = (
            symbol = reservations.destSymbol,
            senderWalletType = WalletType.MAIN,
            senderUuid = walletOwnerManager.systemUuid,
            receiverWalletType = reservations.receiverWalletType,
            receiverUuid = reservations.receiverUuid,
            amount = reservations.reservedDestAmount,
            description = description,
            transferRef = depositRef,
            transferCategory = transferCategory,
            destSymbol = reservations.destSymbol,
            destAmount = reservations.reservedDestAmount
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
        destAmount: BigDecimal = amount,
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
            "Not Set",
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
        finalAmount: BigDecimal?,
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

    private suspend fun validateInitialAmountAndPrecision(
        sourceAmount: BigDecimal,
        sourceSymbol: String
    ) {
        if (sourceAmount == BigDecimal.ZERO) {
            throw OpexError.InvalidAmount.exception("source amount cannot be zero")
        }
        precisionService.validatePrecision(sourceAmount, sourceSymbol)
    }

    private suspend fun fetchRateOrThrow(
        sourceSymbol: String,
        destSymbol: String
    ): Rate {
        return currencyGraph.buildRoutes(sourceSymbol, destSymbol)
            .map { route -> Rate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate()) }
            .firstOrNull() ?: throw OpexError.NOT_EXCHANGEABLE_CURRENCIES.exception()
    }

    private fun calculateDestAmount(
        sourceAmount: BigDecimal,
        rate: Rate
    ): BigDecimal {
        val destAmount = sourceAmount.multiply(rate.rate)
        if (destAmount == BigDecimal.ZERO) {
            throw OpexError.InvalidAmount.exception("dest amount is zero")
        }
        return destAmount
    }

    private suspend fun validateMinimumAmount(
        sourceSymbol: String,
        sourceAmount: BigDecimal,
        destAmount: BigDecimal,
        destSymbol: String,
        rate: BigDecimal
    ) {
        fun minPrecisionAmount(precision: Int): BigDecimal =
            BigDecimal.ONE.scaleByPowerOfTen(-precision)

        val sourcePrecision = precisionService.getPrecision(sourceSymbol).toInt()
        val destPrecision = precisionService.getPrecision(destSymbol).toInt()

        val minSourceAmount = minPrecisionAmount(sourcePrecision)
        val minDestAmount = minPrecisionAmount(destPrecision)

        val minimumSource =
            maxOf(minSourceAmount, minDestAmount.divide(rate, 10, RoundingMode.DOWN)).setScale(
                sourcePrecision,
                RoundingMode.DOWN
            )
        val minimumDest =
            maxOf(minDestAmount, minSourceAmount.multiply(rate)).setScale(destPrecision, RoundingMode.DOWN)

        if (sourceAmount < minimumSource || destAmount < minimumDest) {
            throw OpexError.InvalidMinimumAmount.exception("amount is lower than minimum")
        }
    }

    private suspend fun validateMaximumAmount(
        sourceSymbol: String,
        sourceAmount: BigDecimal,
        destSymbol: String,
        destAmount: BigDecimal,
    ) {
        suspend fun getMaxOrder(symbol: String): BigDecimal {
            return currencyManager.fetchCurrencyMaxOrder(symbol) ?: BigDecimal.ZERO
        }

        if (sourceAmount > getMaxOrder(sourceSymbol) || destAmount > getMaxOrder(destSymbol)) {
            throw OpexError.InvalidMaximumAmount.exception("amount is higher than maximum")
        }
    }
}
