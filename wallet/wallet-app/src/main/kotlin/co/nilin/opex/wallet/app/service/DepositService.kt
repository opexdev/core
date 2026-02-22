package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositRequest
import co.nilin.opex.wallet.app.dto.PaymentDepositResponse
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.DepositType
import co.nilin.opex.wallet.core.spi.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class DepositService(
    private val walletOwnerManager: WalletOwnerManager,
    private val walletManager: WalletManager,
    private val depositPersister: DepositPersister,
    private val transferService: TransferService,
    private val traceDepositService: TraceDepositService,
    private val currencyServiceV2: CurrencyServiceV2,
    private val depositEventSubmitter: DepositEventSubmitter,
    private val transferManager: TransferManager,
    private val currencyService: CurrencyServiceManager,
    @Value("\${app.deposit.snapshot.enabled:true}")
    private val depositSnapshotEnabled: Boolean
) {

    private val logger = LoggerFactory.getLogger(DepositService::class.java)

    // -------------------------------------------------------------------------
    // Helpers (NO LOGIC CHANGE)
    // -------------------------------------------------------------------------

    private suspend fun getOrCreateWalletOwner(ownerUuid: String): WalletOwner {
        return walletOwnerManager.findWalletOwner(ownerUuid)
            ?: walletOwnerManager.createWalletOwner(ownerUuid, "not set", "")
    }

    private suspend fun getOrCreateMainWallet(
        owner: WalletOwner,
        currency: CurrencyCommand
    ): Wallet {
        return walletManager.findWalletByOwnerAndCurrencyAndType(
            owner,
            WalletType.MAIN,
            currency
        ) ?: walletManager.createWallet(
            owner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            WalletType.MAIN
        )
    }

    // -------------------------------------------------------------------------
    // Manual Deposit
    // -------------------------------------------------------------------------

    @Transactional
    suspend fun depositManually(
        symbol: String,
        receiverUuid: String,
        senderUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest,
    ): TransferResult? {

        logger.info(
            "deposit manually: $senderUuid to $receiverUuid on $symbol at ${LocalDateTime.now()}"
        )

        val gateway = currencyServiceV2
            .fetchCurrencyGateway(request.gatewayUuid, symbol)
            ?: throw OpexError.GatewayNotFount.exception()

        if (gateway !is OffChainGatewayCommand || gateway.transferMethod != TransferMethod.MANUALLY) {
            throw OpexError.GatewayNotFount.exception()
        }

        walletOwnerManager.findWalletOwner(senderUuid)
            ?.level
            ?: throw OpexException(OpexError.WalletOwnerNotFound)

        walletOwnerManager.findWalletOwner(receiverUuid)
            ?.level
            ?: walletOwnerManager.createWalletOwner(receiverUuid, "not set", "1").level

        return deposit(
            symbol = symbol,
            receiverUuid = receiverUuid,
            receiverWalletType = WalletType.MAIN,
            senderUuid = senderUuid,
            amount = amount,
            description = request.description,
            transferRef = request.ref,
            chain = null,
            attachment = request.attachment,
            depositType = DepositType.OFF_CHAIN,
            gatewayUuid = request.gatewayUuid,
            transferMethod = TransferMethod.MANUALLY
        )
    }

    // -------------------------------------------------------------------------
    // Core Deposit
    // -------------------------------------------------------------------------

    @Transactional
    suspend fun deposit(
        symbol: String,
        receiverUuid: String,
        receiverWalletType: WalletType,
        senderUuid: String?,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        chain: String?,
        attachment: String?,
        depositType: DepositType,
        gatewayUuid: String?,
        transferMethod: TransferMethod?,
    ): TransferResult? {

        logger.info(
            "A ${depositType.name} deposit tx on $symbol-$chain was received for $receiverUuid ......."
        )

        val depositCommand = Deposit(
            ownerUuid = receiverUuid,
            depositUuid = UUID.randomUUID().toString(),
            currency = symbol,
            amount = amount,
            note = description,
            transactionRef = transferRef,
            status = DepositStatus.DONE,
            depositType = depositType,
            network = chain,
            attachment = attachment,
            transferMethod = transferMethod
        )

        val gatewayData = fetchDepositData(
            gatewayUuid = gatewayUuid,
            symbol = symbol,
            depositType = depositType,
            depositCommand = depositCommand
        )

        if (depositCommand.depositType != depositType) {
            throw OpexError.GatewayNotFount.exception()
        }

        val isValid = isValidDeposit(depositCommand, gatewayData)
        if (!isValid) {
            logger.info(
                "An invalid deposit command : $symbol-$chain-$receiverUuid-$amount"
            )
            depositCommand.status = DepositStatus.INVALID
        }

        traceDepositService.saveDepositInNewTransaction(depositCommand)

        if (!isValid) {
            return null
        }

        logger.info(
            "Going to charge wallet on a ${depositType.name} deposit event :" +
                    "$symbol-$chain-$receiverUuid-$amount"
        )

        val (actualSenderUuid, transferCategory) =
            if (
                senderUuid != null &&
                depositType == co.nilin.opex.wallet.core.model.DepositType.OFF_CHAIN &&
                transferMethod == TransferMethod.MANUALLY
            ) {
                senderUuid to TransferCategory.DEPOSIT_MANUALLY
            } else {
                walletOwnerManager.systemUuid to TransferCategory.DEPOSIT
            }

        val transferResult = transferService.transfer(
            symbol = symbol,
            senderWalletType = WalletType.MAIN,
            senderUuid = actualSenderUuid,
            receiverWalletType = receiverWalletType,
            receiverUuid = receiverUuid,
            amount = amount,
            description = description,
            transferRef = transferRef,
            transferCategory = transferCategory
        ).transferResult

        publishDepositEvent(depositCommand)
        return transferResult
    }

    // -------------------------------------------------------------------------
    // Validation & Gateway
    // -------------------------------------------------------------------------

    fun isValidDeposit(deposit: Deposit, gatewayData: GatewayData): Boolean {
        return gatewayData.isEnabled &&
                deposit.amount >= gatewayData.minimum &&
                deposit.amount <= gatewayData.maximum
    }

    suspend fun fetchDepositData(
        gatewayUuid: String?,
        symbol: String,
        depositType: co.nilin.opex.wallet.core.model.DepositType,
        depositCommand: Deposit,
    ): GatewayData {

        if (gatewayUuid == null) {
            return GatewayData(true, BigDecimal.ZERO, BigDecimal.ZERO, null)
        }

        val gateway = currencyServiceV2
            .fetchCurrencyGateway(gatewayUuid, symbol)
            ?: throw OpexError.GatewayNotFount.exception()

        return when (gateway) {

            is OnChainGatewayCommand -> {
                depositCommand.depositType = co.nilin.opex.wallet.core.model.DepositType.ON_CHAIN
                depositCommand.currency = gateway.currencySymbol!!
                depositCommand.network = gateway.chain

                GatewayData(
                    gateway.isDepositActive ?: true && gateway.depositAllowed ?: true,
                    BigDecimal.ZERO,
                    gateway.depositMin ?: BigDecimal.ZERO,
                    gateway.depositMax
                )
            }

            is OffChainGatewayCommand -> {
                depositCommand.depositType = co.nilin.opex.wallet.core.model.DepositType.OFF_CHAIN
                depositCommand.currency = gateway.currencySymbol!!
                depositCommand.network = null
                depositCommand.transferMethod = gateway.transferMethod

                GatewayData(
                    gateway.isDepositActive ?: true && gateway.depositAllowed ?: true,
                    BigDecimal.ZERO,
                    gateway.depositMin ?: BigDecimal.ZERO,
                    gateway.depositMax
                )
            }

            else -> throw OpexError.GatewayNotFount.exception()
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    suspend fun findDepositHistory(
        uuid: String,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
        size: Int?,
        ascendingByTime: Boolean?,
    ): List<DepositResponse> =
        depositPersister
            .findDepositHistory(uuid, symbol, startTime, endTime, limit, size, ascendingByTime)
            .map {
                DepositResponse(
                    it.depositUuid,
                    it.ownerUuid,
                    it.currency,
                    it.amount,
                    it.network,
                    it.note,
                    it.transactionRef,
                    it.sourceAddress,
                    it.status,
                    it.depositType,
                    it.attachment,
                    it.createDate,
                    it.transferMethod
                )
            }

    suspend fun getDepositHistoryCount(
        uuid: String,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): Long =
        depositPersister.getDepositHistoryCount(uuid, symbol, startTime, endTime)

    suspend fun searchDeposit(
        ownerUuid: String?,
        symbol: String?,
        sourceAddress: String?,
        transactionRef: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: List<DepositStatus>?,
        offset: Int?,
        size: Int?,
        ascendingByTime: Boolean?,
    ): List<DepositAdminResponse> =
        depositPersister.findByCriteria(
            ownerUuid,
            symbol,
            sourceAddress,
            transactionRef,
            startTime,
            endTime,
            status,
            offset,
            size,
            ascendingByTime
        )

    suspend fun getDepositSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): List<TransactionSummary> =
        depositPersister.getDepositSummary(uuid, startTime, endTime, limit)

    // -------------------------------------------------------------------------
    // Payment Deposit
    // -------------------------------------------------------------------------

    suspend fun commitPaymentDeposit(
        request: PaymentDepositRequest
    ): PaymentDepositResponse {

        val currency = currencyService
            .fetchCurrency(FetchCurrency(symbol = request.currency))
            ?: throw OpexError.CurrencyNotFound.exception()

        val sourceOwner = walletOwnerManager
            .findWalletOwner(walletOwnerManager.systemUuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()

        val sourceWallet = getOrCreateMainWallet(sourceOwner, currency)

        val receiverOwner = getOrCreateWalletOwner(request.userId)
        val receiverWallet = getOrCreateMainWallet(receiverOwner, currency)

        transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(currency, request.amount),
                request.description,
                request.reference,
                TransferCategory.DEPOSIT
            )
        )

        val deposit = Deposit(
            ownerUuid = receiverOwner.uuid,
            depositUuid = UUID.randomUUID().toString(),
            currency = currency.symbol,
            amount = request.amount,
            note = request.description,
            transactionRef = request.reference,
            status = DepositStatus.DONE,
            depositType = DepositType.OFF_CHAIN,
            network = null,
            attachment = null,
            transferMethod = request.transferMethod

        )

        traceDepositService.saveDepositInNewTransaction(deposit)
        publishDepositEvent(deposit)

        return PaymentDepositResponse(true)
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    suspend fun publishDepositEvent(deposit: Deposit) {
        if (depositSnapshotEnabled) {
            depositEventSubmitter.send(
                deposit.ownerUuid,
                deposit.transactionRef,
                deposit.currency,
                deposit.amount
            )
        }
    }
}
