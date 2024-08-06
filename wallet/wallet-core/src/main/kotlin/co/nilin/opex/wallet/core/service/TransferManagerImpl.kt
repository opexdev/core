package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.exc.CurrencyNotMatchedException
import co.nilin.opex.wallet.core.exc.DepositLimitExceededException
import co.nilin.opex.wallet.core.exc.NotEnoughBalanceException
import co.nilin.opex.wallet.core.exc.WithdrawLimitExceededException
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.inout.TransferResultDetailed
import co.nilin.opex.wallet.core.model.*
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
    private val transactionManager: TransactionManager,
    private val userTransactionManager: UserTransactionManager,
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
                LocalDateTime.now()
            )
        )
        //TODO make tx long by default
        createUserTX(transferCommand, tx.toLong())

        //get the result and add to return result type
        walletListener.onDeposit(destWallet, srcWallet, transferCommand.amount, amountToTransfer, tx)
        walletListener.onWithdraw(srcWallet, destWallet, transferCommand.amount, tx)
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

    private suspend fun createUserTX(command: TransferCommand, txId: Long) {
        val currency = command.amount.currency.symbol
        val amount = command.amount.amount.toDouble()

        when (command.transferCategory) {
            TransferCategory.TRADE -> {
                val loserOwner = command.sourceWallet.owner.id
                val loserMainWallet = walletManager.findWallet(loserOwner!!, currency, WalletType.MAIN) ?: return
                val loserBalance = loserMainWallet.balance.toDouble()

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount.toDouble()

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    -amount,
                    loserBalance + amount,
                    UserTransactionCategory.TRADE
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    amount,
                    gainerBalance - amount,
                    UserTransactionCategory.TRADE,
                )
                userTransactionManager.save(gainerTx)
            }

            TransferCategory.FEE -> {
                val tx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    command.amount.currency.symbol,
                    -amount,
                    command.sourceWallet.balance.amount.toDouble() + amount,
                    UserTransactionCategory.FEE
                )
                userTransactionManager.save(tx)
            }

            TransferCategory.DEPOSIT, TransferCategory.DEPOSIT_MANUALLY -> {
                val tx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    amount,
                    command.destWallet.balance.amount.toDouble() - amount,
                    UserTransactionCategory.DEPOSIT
                )
                userTransactionManager.save(tx)
            }

            TransferCategory.WITHDRAW_ACCEPT -> {
                val userOwnerId = command.sourceWallet.owner.id!!
                val userWallet = walletManager.findWallet(userOwnerId, currency, WalletType.MAIN) ?: return
                val tx = UserTransaction(
                    userOwnerId,
                    txId,
                    currency,
                    -amount,
                    userWallet.balance.toDouble() + amount,
                    UserTransactionCategory.WITHDRAW
                )
                userTransactionManager.save(tx)
            }

            else -> {
                // No tx needed for other types
            }
        }
    }
}