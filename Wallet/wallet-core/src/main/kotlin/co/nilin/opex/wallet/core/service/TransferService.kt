package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.exc.CurrencyNotMatchedException
import co.nilin.opex.wallet.core.exc.DepositLimitExceededException
import co.nilin.opex.wallet.core.exc.NotEnoughBalanceException
import co.nilin.opex.wallet.core.exc.WithdrawLimitExceededException
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.spi.CurrencyRateService
import co.nilin.opex.wallet.core.spi.TransactionManager
import co.nilin.opex.wallet.core.spi.WalletListener
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date

@Service
class TransferService(
    val currencyRateService: CurrencyRateService,
    val walletManager: WalletManager,
    val walletListener: WalletListener,
    val walletOwnerManager: WalletOwnerManager,
    val transactionManager: TransactionManager
) {
    @Transactional
    suspend fun transfer(transferCommand: TransferCommand): TransferResult {
        //pre transfer hook (dispatch pre transfer event)
        val srcWallet = transferCommand.sourceWallet
        val srcWalletOwner = srcWallet.owner()
        val srcWalletBalance = srcWallet.balance()
        if (srcWallet.currency() != transferCommand.amount.currency)
            throw CurrencyNotMatchedException()
        if (srcWalletBalance.amount < transferCommand.amount.amount)
            throw NotEnoughBalanceException()
        if (!walletOwnerManager.isWithdrawAllowed(srcWalletOwner, transferCommand.amount))
            throw WithdrawLimitExceededException()
        if (!walletManager.isWithdrawAllowed(srcWallet, transferCommand.amount.amount))
            throw WithdrawLimitExceededException()

        val destWallet = transferCommand.destWallet
        val destWalletOwner = destWallet.owner()
        val balance = destWallet.balance()
        //check wallet if can accept the value type
        val amountToTransfer = currencyRateService.convert(transferCommand.amount, destWallet.currency())

        if (!walletOwnerManager.isDepositAllowed(destWalletOwner, Amount(destWallet.currency(), amountToTransfer)))
            throw DepositLimitExceededException()
        if (!walletManager.isDepositAllowed(destWallet, amountToTransfer))
            throw DepositLimitExceededException()

        walletManager.decreaseBalance(srcWallet, transferCommand.amount.amount)
        walletManager.increaseBalance(destWallet, amountToTransfer)
        val tx = transactionManager.save(
            Transaction(
                srcWallet,
                destWallet,
                transferCommand.amount.amount,
                amountToTransfer,
                transferCommand.description,
                transferCommand.transferRef
            )
        )
        //get the result and add to return result type
        walletListener.onDeposit(destWallet, srcWallet, transferCommand.amount, amountToTransfer, tx, transferCommand.additionalData)
        walletListener.onWithdraw(srcWallet, destWallet, transferCommand.amount, tx, transferCommand.additionalData)
        //post transfer hook(dispatch post transfer event)

        //notify balance change
        return TransferResult(
            Date().time, srcWalletOwner.uuid()
            , srcWallet.type()
            , srcWalletBalance
            , walletManager.findWalletById(srcWallet.id()!!)!!.balance()
            , transferCommand.amount
            , destWalletOwner.uuid()
            , destWallet.type()
            , Amount(destWallet.currency(), amountToTransfer)
        )
    }
}