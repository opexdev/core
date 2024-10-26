package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.WithdrawType
import co.nilin.opex.wallet.core.spi.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime


enum class WithDrawAction { WITHDRAW_REQUEST, WITHDRAW_ACCEPT, WITHDRAW_REJECT }

@Service
class WithdrawService(
    private val withdrawPersister: WithdrawPersister,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val currencyService: CurrencyServiceManager,
    private val transferManager: TransferManager,
    private val authService: AuthService,
    private val environment: Environment,
    private val gatewayService: GatewayService,
    @Qualifier("onChainGateway") private val bcGatewayProxy: GatewayPersister,
    @Value("\${app.system.uuid}") private val systemUuid: String
) {
    private val logger = LoggerFactory.getLogger(WithdrawService::class.java)


    @Transactional
    suspend fun requestWithdraw(withdrawCommand: WithdrawCommand): WithdrawActionResult {


        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdrawCommand.currency))
            ?: throw OpexError.CurrencyNotFound.exception()
        val owner = walletOwnerManager.findWalletOwner(withdrawCommand.uuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
                ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            owner, WalletType.CASHOUT, currency
        ) ?: walletManager.createWallet(
            owner,
            Amount(currency, BigDecimal.ZERO),
            currency,
            WalletType.CASHOUT
        )
        val withdrawData: GatewayData =
            _fetchWithdrawData(withdrawCommand) ?: throw OpexError.GatewayNotFount.exception()
        if (!withdrawData.isEnabled)
            throw OpexError.WithdrawNotAllowed.exception()

        val withdrawFee = withdrawData.fee
        val realAmount = withdrawCommand.amount - withdrawFee

        if (withdrawCommand.amount > sourceWallet.balance.amount + withdrawFee)
            throw OpexError.WithdrawAmountExceedsWalletBalance.exception()

        if (withdrawCommand.amount < withdrawData.minimum)
            throw OpexError.WithdrawAmountLessThanMinimum.exception()

        if (withdrawCommand.amount > withdrawData.maximum)
            throw OpexError.WithdrawAmountMoreThanMinimum.exception()


        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(currency, withdrawCommand.amount),
                withdrawCommand.description,
                "wallet:withdraw:${owner.uuid}:${WithdrawStatus.CREATED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_REQUEST
            )
        )

        val withdraw = withdrawPersister.persist(
            Withdraw(
                null,
                owner.uuid,
                currency.symbol,
                receiverWallet.id!!,
                realAmount,
                transferResultDetailed.tx,
                null,
                withdrawFee,
                null,
                withdrawCommand.destSymbol,
                withdrawCommand.destAddress,
                withdrawCommand.destNetwork,
                withdrawCommand.destNote,
                null,
                null,
                WithdrawStatus.CREATED,
                null,
                withdrawCommand.withdrawType!!,
                null
            )
        )

        return WithdrawActionResult(withdraw.withdrawId!!, withdraw.status)
    }


    suspend fun _fetchWithdrawData(withdrawCommand: WithdrawCommand): GatewayData? {

        return withdrawCommand.gatewayUuid?.let { uuid ->

            gatewayService.fetchGateway(uuid, withdrawCommand.currency)?.let {

                when (it) {
                    is OnChainGatewayCommand -> {
                        withdrawCommand.destNetwork = it.chain
                        withdrawCommand.destSymbol = it.implementationSymbol!!
                        withdrawCommand.withdrawType = WithdrawType.ON_CHAIN
                        GatewayData(
                            it.isActive ?: true && it.withdrawAllowed ?: true,
                            it.withdrawFee ?: BigDecimal.ZERO,
                            it.withdrawMin ?: BigDecimal.ZERO,
                            it.withdrawMax
                        )
                    }

                    is OffChainGatewayCommand -> {
                        withdrawCommand.destNetwork = it.transferMethod.name
                        withdrawCommand.withdrawType = WithdrawType.OFF_CHAIN
                        GatewayData(
                            it.isActive ?: true && it.withdrawAllowed ?: true,
                            it.withdrawFee ?: BigDecimal.ZERO,
                            it.withdrawMin ?: BigDecimal.ZERO,
                            it.withdrawMax
                        )
                    }

                    else -> {
                        throw OpexError.GatewayNotFount.exception()
                    }
                }
            } ?: throw OpexError.GatewayNotFount.exception()

            //After applying gateway concept in ope, we can remove this line and
            // use gatewayUUid instead of combination of symbol and network
        } ?: bcGatewayProxy.getWithdrawData(withdrawCommand.destSymbol!!, withdrawCommand.destNetwork!!)
    }

    @Transactional
    suspend fun acceptWithdraw(acceptCommand: WithdrawAcceptCommand): WithdrawActionResult {
        val system = walletOwnerManager.findWalletOwner(systemUuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val withdraw = withdrawPersister.findById(acceptCommand.withdrawId)
            ?: throw OpexError.WithdrawNotFound.exception()

        if (!withdraw.canBeAccepted())
            throw OpexError.WithdrawAlreadyProcessed.exception()

        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(system, WalletType.MAIN, sourceWallet.currency)
                ?: walletManager.createWallet(
                    system,
                    Amount(sourceWallet.currency, BigDecimal.ZERO),
                    sourceWallet.currency,
                    WalletType.MAIN
                )

        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, withdraw.amount + withdraw.appliedFee),
                null,
                "wallet:withdraw:${sourceWallet.owner.uuid}:${WithdrawStatus.DONE}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_ACCEPT
            )
        )

        val updateWithdraw = withdrawPersister.persist(
            Withdraw(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                transferResultDetailed.tx,
                withdraw.appliedFee,
                acceptCommand.destAmount ?: withdraw.amount,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                withdraw.destNote ?: acceptCommand.destNote,
                acceptCommand.destTransactionRef,
                null,
                WithdrawStatus.DONE,
                withdraw.applicator,
                withdraw.withdrawType,
                acceptCommand.attachment,
                withdraw.createDate,
                LocalDateTime.now(),
            )
        )

        return WithdrawActionResult(updateWithdraw.withdrawId!!, updateWithdraw.status)
    }

    suspend fun processWithdraw(withdrawId: Long): WithdrawActionResult {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()

        if (!withdraw.canBeProcessed())
            throw OpexError.WithdrawAlreadyProcessed.exception()

        withdraw.status = WithdrawStatus.PROCESSING
        withdrawPersister.persist(withdraw)

        return WithdrawActionResult(withdraw.withdrawId!!, WithdrawStatus.PROCESSING)
    }

    @Transactional
    suspend fun cancelWithdraw(uuid: String, withdrawId: Long) {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
        if (withdraw.ownerUuid != uuid) throw OpexError.Forbidden.exception()
        if (!withdraw.canBeCanceled()) throw OpexError.WithdrawCannotBeCanceled.exception()

        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdraw.currency))
            ?: throw OpexError.CurrencyNotFound.exception()
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.CASHOUT, currency)
            ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            ?: throw OpexError.WalletNotFound.exception()

        withdraw.status = WithdrawStatus.CANCELED
        withdrawPersister.persist(withdraw)

        transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(currency, withdraw.amount + withdraw.appliedFee),
                null,
                "wallet:withdraw:${withdraw.withdrawId}:${WithdrawStatus.CANCELED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_CANCEL
            )
        )
    }

    @Transactional
    suspend fun rejectWithdraw(rejectCommand: WithdrawRejectCommand): WithdrawActionResult {
        val withdraw = withdrawPersister.findById(rejectCommand.withdrawId)
            ?: throw OpexError.WithdrawNotFound.exception()

        if (!withdraw.canBeRejected())
            throw OpexError.WithdrawCannotBeRejected.exception()

        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            sourceWallet.owner,
            WalletType.MAIN,
            sourceWallet.currency
        ) ?: walletManager.createWallet(
            sourceWallet.owner,
            Amount(sourceWallet.currency, BigDecimal.ZERO),
            sourceWallet.currency,
            WalletType.MAIN
        )

        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, withdraw.amount + withdraw.appliedFee),
                rejectCommand.statusReason,
                "wallet:withdraw:${withdraw.withdrawId}:${WithdrawStatus.REJECTED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_REJECT
            )
        )
        val updateWithdraw = withdrawPersister.persist(
            Withdraw(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                transferResultDetailed.tx,
                withdraw.appliedFee,
                null,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                withdraw.destNote,
                null,
                rejectCommand.statusReason,
                WithdrawStatus.REJECTED,
                withdraw.applicator,
                withdraw.withdrawType,
                withdraw.attachment,
                withdraw.createDate,
                LocalDateTime.now(),
            )
        )
        return WithdrawActionResult(withdraw.withdrawId!!, updateWithdraw.status)
    }

    suspend fun findWithdraw(id: Long): WithdrawResponse? {
        return withdrawPersister.findWithdrawResponseById(id)
    }

    suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean,
        offset: Int,
        size: Int
    ): List<WithdrawResponse> {
        return withdrawPersister.findByCriteria(
            ownerUuid,
            currency,
            destTxRef,
            destAddress,
            status,
            startTime,
            endTime,
            ascendingByTime,
            offset,
            size
        )
    }

//    suspend fun findByCriteria(
//        ownerUuid: String?,
//        currency: String?,
//        destTxRef: String?,
//        destAddress: String?,
//        status: List<WithdrawStatus>,
//    ): List<WithdrawResponse> {
//        return withdrawPersister.findByCriteria(
//            ownerUuid,
//            currency,
//            destTxRef,
//            destAddress,
//            status
//        )
//    }

    suspend fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false
    ): List<WithdrawResponse> {
        return withdrawPersister.findWithdrawHistory(uuid, currency, startTime, endTime, limit, offset, ascendingByTime)
    }


}