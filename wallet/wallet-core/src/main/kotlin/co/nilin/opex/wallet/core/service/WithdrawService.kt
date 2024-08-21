package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class WithdrawService(
    private val withdrawPersister: WithdrawPersister,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val currencyService: CurrencyService,
    private val transferManager: TransferManager,
    @Value("\${app.system.uuid}") private val systemUuid: String
) {

    @Transactional
    suspend fun requestWithdraw(withdrawCommand: WithdrawCommand): WithdrawResult {
        val currency = currencyService.getCurrency(withdrawCommand.currency)
            ?: throw OpexError.CurrencyNotFound.exception()
        val owner = walletOwnerManager.findWalletOwner(withdrawCommand.uuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.CASHOUT, currency)
            ?: walletManager.createCashoutWallet(owner, currency)

        if (withdrawCommand.amount + withdrawCommand.fee > sourceWallet.balance.amount)
            throw OpexError.WithdrawAmountExceedsWalletBalance.exception()

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
                withdrawCommand.amount,
                transferResultDetailed.tx,
                null,
                withdrawCommand.fee,
                null,
                withdrawCommand.destSymbol,
                withdrawCommand.destAddress,
                withdrawCommand.destNetwork,
                withdrawCommand.destNote,
                null,
                null,
                WithdrawStatus.CREATED
            )
        )

        return WithdrawResult(withdraw.withdrawId!!, withdraw.status)
    }

    @Transactional
    suspend fun acceptWithdraw(acceptCommand: WithdrawAcceptCommand): WithdrawResult {
        val system = walletOwnerManager.findWalletOwner(systemUuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val withdraw = withdrawPersister.findById(acceptCommand.withdrawId)
            ?: throw OpexError.WithdrawNotFound.exception()

        if (withdraw.status != WithdrawStatus.CREATED)
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
                Amount(sourceWallet.currency, withdraw.amount),
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
                acceptCommand.destAmount,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                //TODO why ----- ?
                withdraw.destNote ?: ("" + "-----------" + (acceptCommand.destNote ?: "")),
                acceptCommand.destTransactionRef,
                null,
                WithdrawStatus.DONE,
                withdraw.createDate,
                LocalDateTime.now()
            )
        )

        return WithdrawResult(updateWithdraw.withdrawId!!, updateWithdraw.status)
    }

    @Transactional
    suspend fun rejectWithdraw(rejectCommand: WithdrawRejectCommand): WithdrawResult {
        val withdraw = withdrawPersister.findById(rejectCommand.withdrawId)
            ?: throw OpexError.WithdrawNotFound.exception()

        if (withdraw.status != WithdrawStatus.CREATED)
            throw OpexError.WithdrawAlreadyProcessed.exception()

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
                Amount(sourceWallet.currency, withdraw.amount),
                rejectCommand.statusReason,
                null,
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
                null,
                null,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                //TODO why ----- ?
                withdraw.destNote ?: ("" + "-----------" + (rejectCommand.destNote ?: "")),
                null,
                rejectCommand.statusReason,
                WithdrawStatus.REJECTED,
                withdraw.createDate,
                null
            )
        )
        return WithdrawResult(withdraw.withdrawId!!, updateWithdraw.status)
    }

    suspend fun findWithdraw(id: Long): WithdrawResponse? {
        return withdrawPersister.findWithdrawResponseById(id)
    }

    suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?,
        offset: Int,
        size: Int
    ): PagingWithdrawResponse {
        val count = withdrawPersister.countByCriteria(ownerUuid, currency, destTxRef, destAddress, noStatus, status)
        val list = withdrawPersister.findByCriteria(
            ownerUuid,
            currency,
            destTxRef,
            destAddress,
            noStatus,
            status,
            offset,
            size
        )
        return PagingWithdrawResponse(count, list)
    }

    suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?,
    ): List<WithdrawResponse> {
        return withdrawPersister.findByCriteria(
            ownerUuid,
            currency,
            destTxRef,
            destAddress,
            noStatus,
            status
        )
    }

    suspend fun findWithdrawHistory(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false
    ): List<Withdraw> {
        return withdrawPersister.findWithdrawHistory(uuid, coin, startTime, endTime, limit, offset, ascendingByTime)
    }

}