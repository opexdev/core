package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.exc.CurrencyNotMatchedException
import co.nilin.opex.wallet.core.exc.DepositLimitExceededException
import co.nilin.opex.wallet.core.exc.NotEnoughBalanceException
import co.nilin.opex.wallet.core.exc.WithdrawLimitExceededException
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.inout.TransferResultDetailed
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.spi.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
class TransferManagerImpl(
    private val walletManager: WalletManager,
    private val walletListener: WalletListener,
    private val walletOwnerManager: WalletOwnerManager,
    private val transactionManager: TransactionManager
) : TransferManager {
    private val logger = LoggerFactory.getLogger(TransferManagerImpl::class.java)

    @Transactional
    override suspend fun transfer(transferCommand: TransferCommand): TransferResultDetailed {
        //pre transfer hook (dispatch pre transfer event)
        val srcWallet = transferCommand.sourceWallet
        val srcWalletOwner = srcWallet.owner
        val srcWalletBalance = srcWallet.balance

        //todo need to review(compare symbols instead of objects)
        if (srcWallet.currency.symbol != transferCommand.amount.currency.symbol)
            throw OpexError.BadRequest.exception()
        if (srcWalletBalance.amount < transferCommand.amount.amount)
            throw OpexError.NotEnoughBalance.exception()
        if (!walletOwnerManager.isWithdrawAllowed(srcWalletOwner, transferCommand.amount))
            throw OpexError.WithdrawNotAllowed.exception()
        if (!walletManager.isWithdrawAllowed(srcWallet, transferCommand.amount.amount))
            throw OpexError.WithdrawNotAllowed.exception()

        val destWallet = transferCommand.destWallet
        val destWalletOwner = destWallet.owner
        //todo need to review(compare symbols instead of objects)
        if (destWallet.currency.symbol != transferCommand.destAmount.currency.symbol)
            throw OpexError.BadRequest.exception()
        //check wallet if it can accept the value type
        val amountToTransfer = transferCommand.destAmount.amount

        if (!walletOwnerManager.isDepositAllowed(destWalletOwner, Amount(destWallet.currency, amountToTransfer)))
            throw OpexError.DepositLimitExceeded.exception()
        if (!walletManager.isDepositAllowed(destWallet, amountToTransfer))
            throw OpexError.DepositLimitExceeded.exception()

        walletManager.decreaseBalance(srcWallet, transferCommand.amount.amount)
        walletManager.increaseBalance(destWallet, amountToTransfer)
        val tx = transactionManager.save(
            Transaction(
                srcWallet,
                destWallet,
                transferCommand.amount.amount,
                amountToTransfer,
                transferCommand.description,
                transferCommand.transferRef,
                transferCommand.transferCategory,
                transferCommand.additionalData,
                LocalDateTime.now()
            )
        )
        //get the result and add to return result type
        walletListener.onDeposit(
            destWallet,
            srcWallet,
            transferCommand.amount,
            amountToTransfer,
            tx,
            transferCommand.additionalData
        )
        walletListener.onWithdraw(srcWallet, destWallet, transferCommand.amount, tx, transferCommand.additionalData)
        //post transfer hook(dispatch post transfer event)

        //notify balance change
        return TransferResultDetailed(
            TransferResult(
                Date().time,
                srcWalletOwner.uuid,
                srcWallet.type,
                srcWalletBalance,
                walletManager.findWalletById(srcWallet.id!!)!!.balance,
                transferCommand.amount,
                destWalletOwner.uuid,
                destWallet.type,
                Amount(destWallet.currency, amountToTransfer)
            ), tx
        )
    }
}