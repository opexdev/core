package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.spi.*
import org.springframework.beans.factory.annotation.Value
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
        @Value("\${app.system.uuid}") private val systemUuid: String
) {


    @Transactional
    suspend fun requestWithdraw(withdrawCommand: WithdrawCommand): WithdrawResult {

        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdrawCommand.currency))
                ?: throw OpexError.CurrencyNotFound.exception()
        val owner = walletOwnerManager.findWalletOwner(withdrawCommand.uuid)
                ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet =
                walletManager.findWalletByOwnerAndCurrencyAndType(owner, "main", currency)
                        ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
                owner, "cashout", currency
        ) ?: walletManager.createWallet(
                owner,
                Amount(currency, BigDecimal.ZERO),
                currency,
                "cashout"
        )
        val transferResultDetailed = transferManager.transfer(
                TransferCommand(
                        sourceWallet,
                        receiverWallet,
                        Amount(currency, withdrawCommand.amount),
                        withdrawCommand.description,
                        withdrawCommand.transferRef,
                        WithDrawAction.WITHDRAW_REQUEST.name,
                        emptyMap()
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
                        withdrawCommand.acceptedFee,
                        null,
                        null,
                        withdrawCommand.destSymbol,
                        withdrawCommand.destAddress,
                        withdrawCommand.destNetwork,
                        withdrawCommand.destNote,
                        null,
                        null,
                        "CREATED"
                )
        )
        return WithdrawResult(withdraw.withdrawId!!, withdraw.status)
    }

    @Transactional
    suspend fun acceptWithdraw(acceptCommand: WithdrawAcceptCommand): WithdrawResult {
        val system = walletOwnerManager.findWalletOwner(systemUuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val withdraw = withdrawPersister.findById(acceptCommand.withdrawId)
                ?: throw RuntimeException("No matching withdraw request")
        if (withdraw.status != "CREATED") {
            throw RuntimeException("This withdraw request processed before")
        }
        if (withdraw.acceptedFee < acceptCommand.appliedFee) {
            throw RuntimeException("Applied Fee ${acceptCommand.appliedFee} is bigger than accepted Fee ${withdraw.acceptedFee}")
        }
        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
                system, "main", sourceWallet.currency
        ) ?: walletManager.createWallet(
                system,
                Amount(sourceWallet.currency, BigDecimal.ZERO),
                sourceWallet.currency,
                "main"
        )
        val transferResultDetailed = transferManager.transfer(
                TransferCommand(
                        sourceWallet,
                        receiverWallet,
                        Amount(sourceWallet.currency, withdraw.amount),
                        null, null, WithDrawAction.WITHDRAW_ACCEPT.name, emptyMap()
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
                        withdraw.acceptedFee,
                        withdraw.appliedFee,
                        withdraw.amount.subtract(acceptCommand.appliedFee),
                        withdraw.destSymbol,
                        withdraw.destAddress,
                        withdraw.destNetwork,
                        withdraw.destNote ?: acceptCommand.destNote ?: "",
                        acceptCommand.destTransactionRef!!,
                        null,
                        "DONE",
                        withdraw.createDate,
                        LocalDateTime.now(),
                        acceptCommand.applicator
                )
        )

        return WithdrawResult(withdraw.withdrawId!!, updateWithdraw.status)

    }

    @Transactional
    suspend fun rejectWithdraw(rejectCommand: WithdrawRejectCommand): WithdrawResult {
        val withdraw = withdrawPersister.findById(rejectCommand.withdrawId)
                ?: throw OpexError.WithdrawNotFound.exception()
        if (withdraw.status != "CREATED") {
            throw OpexError.InvalidWithdrawStatus.exception()
        }
        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
                sourceWallet.owner, "main", sourceWallet.currency
        ) ?: walletManager.createWallet(
                sourceWallet.owner,
                Amount(sourceWallet.currency, BigDecimal.ZERO),
                sourceWallet.currency,
                "main"
        )
        val transferResultDetailed = transferManager.transfer(
                TransferCommand(
                        sourceWallet,
                        receiverWallet,
                        Amount(sourceWallet.currency, withdraw.amount),
                        rejectCommand.statusReason, null, WithDrawAction.WITHDRAW_REJECT.name, emptyMap()
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
                        withdraw.acceptedFee,
                        null,
                        null,
                        withdraw.destSymbol,
                        withdraw.destAddress,
                        withdraw.destNetwork,
                        withdraw.destNote ?: ("" + "-----------" + (rejectCommand.destNote ?: "")),
                        null,
                        rejectCommand.statusReason,
                        "REJECTED",
                        withdraw.createDate,
                        LocalDateTime.now(),
                        rejectCommand.applicator
                )
        )
        return WithdrawResult(withdraw.withdrawId!!, updateWithdraw.status)
    }

    suspend fun findByCriteria(
            ownerUuid: String?,
            withdrawId: String?,
            currency: String?,
            destTxRef: String?,
            destAddress: String?,
            noStatus: Boolean,
            status: List<String>?,
            offset: Int,
            size: Int,
            ascendingByTime: Boolean
    ): PagingWithdrawResponse {
        val count =
                withdrawPersister.countByCriteria(ownerUuid, withdrawId, currency, destTxRef, destAddress, noStatus, status)
        val list = withdrawPersister.findByCriteria(
                ownerUuid,
                withdrawId,
                currency,
                destTxRef,
                destAddress,
                noStatus,
                status,
                offset,
                size,
                ascendingByTime
        )
        return PagingWithdrawResponse(count, list)
    }

    suspend fun findByCriteria(
            ownerUuid: String? = null,
            withdrawId: String? = null,
            currency: String? = null,
            destTxRef: String? = null,
            destAddress: String? = null,
            noStatus: Boolean = true,
            status: List<String>? = null,
    ): List<WithdrawResponse> {
        return withdrawPersister.findByCriteria(
                ownerUuid,
                withdrawId,
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