package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.preferences.Preferences
import co.nilin.opex.wallet.app.dto.AdvanceReservedTransferData
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.dto.TransferRequest
import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.GatewayType
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Service
class TransferService(
    private val transferManager: TransferManager,
    private val currencyManager: CurrencyServiceManager,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val currencyGraph: GraphService,
    private val reservedTransferManager: ReservedTransferManager,
    private val depositPersister: DepositPersister,
    private val withdrawPersister: WithdrawPersister,
    private val currencyService: CurrencyServiceV2

) {

    @Autowired
    private lateinit var preferences: Preferences

    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    val reserved: MutableMap<String, AdvanceReservedTransferData> = mutableMapOf()

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
    ): TransferResult {
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


    @Transactional
    suspend fun deposit(
        symbol: String,
        receiverUuid: String,
        receiverWalletType: WalletType,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        chain: String?
    ): TransferResult {

        val tx = _transfer(
            symbol,
            WalletType.MAIN,
            walletOwnerManager.systemUuid,
            receiverWalletType,
            receiverUuid,
            amount,
            description,
            transferRef,
            TransferCategory.DEPOSIT,
            symbol,
            amount

        )

        depositPersister.persist(
            Deposit(
                receiverUuid,
                UUID.randomUUID().toString(),
                symbol,
                amount,
                note = description,
                transactionRef = transferRef,
                status = DepositStatus.DONE,
                depositType = DepositType.ON_CHAIN,
                network = chain
            )
        )

        return tx
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
                reservedDestAmount = finalAmount
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
    ): TransferResult {
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
        )

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
        )

        reservedTransferManager.commitReserve(reserveNumber)
        return TransferResult(
            senderTransfer.date,
            senderTransfer.sourceUuid,
            senderTransfer.sourceWalletType,
            senderTransfer.sourceBalanceBeforeAction,
            senderTransfer.sourceBalanceAfterAction,
            senderTransfer.amount,
            receiverTransfer.destUuid,
            receiverTransfer.destWalletType,
            receiverTransfer.receivedAmount
        )
    }


    @Transactional
    suspend fun depositManually(
        symbol: String,
        receiverUuid: String,
        senderUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult {
        logger.info("deposit manually: $senderUuid to $receiverUuid on $symbol at ${LocalDateTime.now()}")
        val systemUuid = "1"
        //todo customize error message
        if (!isManualWithdrawAllowed(symbol))
            throw OpexError.GatewayNotFount.exception()
        val senderLevel = walletOwnerManager.findWalletOwner(senderUuid)?.let { it.level }
            ?: throw OpexException(OpexError.WalletOwnerNotFound)
        walletOwnerManager.findWalletOwner(receiverUuid)?.let { it.level }
            ?: walletOwnerManager.createWalletOwner(
                receiverUuid,
                "not set",
                "1"
            ).level


        val tx = _transfer(
            symbol,
            WalletType.MAIN,
            senderUuid,
            WalletType.MAIN,
            receiverUuid,
            amount,
            request.description,
            request.ref,
            TransferCategory.DEPOSIT_MANUALLY,
            symbol,
            amount

        )
        depositPersister.persist(
            Deposit(
                receiverUuid,
                UUID.randomUUID().toString(),
                symbol,
                amount,
                note = request.description,
                transactionRef = request.ref,
                status = DepositStatus.DONE,
                depositType = DepositType.SYSTEM,
            )
        )
        return tx
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
    ): TransferResult {
        if (senderWalletType == WalletType.CASHOUT || receiverWalletType == WalletType.CASHOUT)
            throw OpexError.InvalidCashOutUsage.exception()
        val sourceCurrency = currencyManager.fetchCurrency(FetchCurrency(symbol = destSymbol))
            ?: throw OpexError.CurrencyNotFound.exception()
        val sourceOwner = walletOwnerManager.findWalletOwner(senderUuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner, senderWalletType, sourceCurrency)
                ?: throw OpexError.WalletNotFound.exception()

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
        ).transferResult
    }

    @Transactional
    suspend fun withdrawManually(
        symbol: String,
        receiverUuid: String,
        sourceUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult {
        logger.info("withdraw manually: $sourceUuid to $receiverUuid on $symbol at ${LocalDateTime.now()}")

        if (!isManualDepositAllowed(symbol))
            throw OpexError.GatewayNotFount.exception()

        walletOwnerManager.findWalletOwner(receiverUuid)
            ?: walletOwnerManager.createWalletOwner(
                receiverUuid,
                "admin", "admin"
            )

        val tx = _transfer(
            symbol,
            WalletType.MAIN,
            sourceUuid,
            WalletType.MAIN,
            receiverUuid,
            amount,
            request.description,
            request.ref,
            TransferCategory.WITHDRAW_MANUALLY,
            symbol,
            amount
        )
        //todo need to review
        withdrawPersister.persist(
            Withdraw(
                null,
                sourceUuid,
                symbol,
                tx.destWallet!!,
                amount,
                //it should be replaced with tx.id
                request.ref!!,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                symbol,
                receiverUuid,
                null,
                request.description,
                request.ref,
                null,
                WithdrawStatus.SYSTEM,
                receiverUuid
            )
        )
        return tx;
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

    internal suspend fun isManualDepositAllowed(symbol: String): Boolean {
        return currencyService.fetchCurrencyWithGateways(symbol, listOf(GatewayType.Manually))?.depositAllowed ?: false
    }


    internal suspend fun isManualWithdrawAllowed(symbol: String): Boolean {
        return currencyService.fetchCurrencyWithGateways(symbol, listOf(GatewayType.Manually))?.withdrawAllowed ?: false
    }

}