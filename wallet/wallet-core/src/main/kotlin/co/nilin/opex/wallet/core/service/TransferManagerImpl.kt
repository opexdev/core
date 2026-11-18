package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.inout.TransferResultDetailed
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.*
import java.math.BigDecimal
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
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
    private val accountingTransactionEventSubmitter: AccountingTransactionEventSubmitter,
    private val swapEventSubmitter: SwapEventSubmitter? = null,
) : TransferManager {

    private val logger = LoggerFactory.getLogger(TransferManagerImpl::class.java)

    @Transactional
    override suspend fun transfer(transferCommand: TransferCommand): TransferResultDetailed {
        resolveIdempotentTransfer(transferCommand)?.let { return it }

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

        val tx = try {
            transactionManager.save(
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
        } catch (e: DuplicateKeyException) {
            resolveIdempotentTransfer(transferCommand)?.let { return it }
            throw e
        }

        walletManager.decreaseBalance(srcWallet, transferCommand.amount.amount)
        walletManager.increaseBalance(destWallet, amountToTransfer)
        //TODO make tx long by default
        createUserTX(transferCommand, tx)

        //get the result and add to return result type
        walletListener.onDeposit(destWallet, srcWallet, transferCommand.amount, amountToTransfer, tx.toString())
        walletListener.onWithdraw(srcWallet, destWallet, transferCommand.amount, tx.toString())
        emitAccountingEventForTransfer(transferCommand, tx.toString())
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
                Amount(destWallet.currency, amountToTransfer),
                srcWallet.id,
                destWallet.id,
            ), tx.toString()
        )
    }

    private suspend fun resolveIdempotentTransfer(transferCommand: TransferCommand): TransferResultDetailed? {
        val transferRef = transferCommand.transferRef ?: return null
        val persistedTransaction = transactionManager.findTransactionByTransferRef(transferRef) ?: return null
        val existingTxId = persistedTransaction.id
        val existingTransaction = persistedTransaction.transaction

        if (!matchesIdempotentTransfer(transferCommand, existingTransaction)) {
            throw OpexError.BadRequest.exception("transferRef=$transferRef already exists with different parameters")
        }

        logger.info("Idempotent transfer hit for transferRef={}", transferRef)
        return buildIdempotentResult(existingTransaction, existingTxId)
    }

    private fun matchesIdempotentTransfer(transferCommand: TransferCommand, existingTransaction: Transaction): Boolean {
        return transferCommand.sourceWallet.id == existingTransaction.sourceWallet.id &&
            transferCommand.destWallet.id == existingTransaction.destWallet.id &&
            transferCommand.amount == Amount(existingTransaction.sourceWallet.currency, existingTransaction.sourceAmount) &&
            transferCommand.destAmount == Amount(existingTransaction.destWallet.currency, existingTransaction.destAmount) &&
            transferCommand.transferCategory == existingTransaction.transferCategory &&
            transferCommand.description == existingTransaction.description
    }

    private fun buildIdempotentResult(existingTransaction: Transaction, existingTxId: Long): TransferResultDetailed {
        val srcWallet = existingTransaction.sourceWallet
        val destWallet = existingTransaction.destWallet
        return TransferResultDetailed(
            TransferResult(
                Date().time,
                srcWallet.owner.uuid,
                srcWallet.type,
                srcWallet.balance,
                srcWallet.balance,
                Amount(srcWallet.currency, existingTransaction.sourceAmount),
                destWallet.owner.uuid,
                destWallet.type,
                Amount(destWallet.currency, existingTransaction.destAmount),
                srcWallet.id,
                destWallet.id,
            ),
            existingTxId.toString()
        )
    }

    private suspend fun emitAccountingEventForTransfer(transferCommand: TransferCommand, txId: String) {
        val sourceUserId = transferCommand.sourceWallet.owner.uuid
        val destUserId = transferCommand.destWallet.owner.uuid
        val asset = transferCommand.amount.currency.symbol
        val amount = transferCommand.amount.amount
        val transferCategory = transferCommand.transferCategory.name
        val operationId = transferCommand.operationId ?: extractWithdrawOperationId(transferCommand.transferRef)
        val withdrawStatus = withdrawStatusForCategory(transferCommand.transferCategory)

        val eventType = when (transferCommand.transferCategory) {
            TransferCategory.TRADE -> "TRADE"
            TransferCategory.FEE -> "FEE"
            TransferCategory.PURCHASE_FINALIZED,
            TransferCategory.IMPERSONATED_PURCHASE_FINALIZED -> "SWAP"
            TransferCategory.REFERRAL_COMMISSION,
            TransferCategory.REFERENT_COMMISSION -> "REFERRAL"
            TransferCategory.REFERRAL_KYC_REWARD,
            TransferCategory.KYC_ACCEPTED_REWARD -> "REWARD"
            TransferCategory.DEPOSIT,
            TransferCategory.DEPOSIT_MANUALLY -> "DEPOSIT"
            TransferCategory.WITHDRAW_ACCEPT,
            TransferCategory.WITHDRAW_MANUALLY -> "WITHDRAW"
            // Requests, cancellations and rejections have no external financial outflow.
            // Their history is captured by the dedicated withdrawal lifecycle publisher.
            else -> return
        }
        // Direction belongs to a named party, not to an operation category.
        val perspective = transferCommand.perspectivePartyId ?: when (eventType) {
            "DEPOSIT", "REFERRAL", "REWARD" -> destUserId
            else -> sourceUserId
        }
        val side = if (sourceUserId == destUserId && perspective == sourceUserId) "INTERNAL" else when (perspective) {
            sourceUserId -> "OUT"
            destUserId -> "IN"
            else -> null
        }
        val now = LocalDateTime.now()

        accountingTransactionEventSubmitter.send(
            AccountingTransactionEvent(
                txId = txId,
                eventId = "wallet-transfer:$txId",
                userId = perspective,
                type = eventType,
                symbol = transferCommand.amount.currency.symbol,
                asset = asset,
                amount = amount,
                fee = null,
                feeAsset = asset,
                principalAmount = null,
                grossAmount = amount,
                withdrawStatus = withdrawStatus,
                transferCategory = transferCategory,
                operationId = operationId,
                financialActionId = transferCommand.transferRef
                    ?.takeIf { it.startsWith("accountant:fiActions:") }
                    ?.removePrefix("accountant:fiActions:"),
                businessReference = transferCommand.transferRef,
                walletTransactionRef = txId,
                recordRole = when (eventType) {
                    "SWAP" -> "SWAP_SETTLEMENT"
                    "TRADE" -> "TRADE_SETTLEMENT"
                    "FEE" -> "FEE_SETTLEMENT"
                    "WITHDRAW" -> withdrawRecordRoleForCategory(transferCommand.transferCategory, eventType)
                    else -> "TRANSFER"
                },
                senderPartyId = sourceUserId,
                receiverPartyId = destUserId,
                senderWalletId = transferCommand.sourceWallet.id?.toString(),
                receiverWalletId = transferCommand.destWallet.id?.toString(),
                senderWalletType = transferCommand.sourceWallet.type.name,
                receiverWalletType = transferCommand.destWallet.type.name,
                perspectivePartyId = perspective,
                sourceAsset = asset,
                sourceAmount = amount,
                destinationAsset = transferCommand.destAmount.currency.symbol,
                destinationAmount = transferCommand.destAmount.amount,
                source = "CORE_WALLET",
                referenceId = txId,
                side = side,
                price = null,
                quantity = null,
                operationCreatedAt = null,
                lifecycleTransitionAt = now,
                completedAt = null,
                occurredAt = now
            )
        )
    }

    private fun withdrawStatusForCategory(category: TransferCategory): String? {
        return when (category) {
            TransferCategory.WITHDRAW_ACCEPT,
            TransferCategory.WITHDRAW_MANUALLY -> WithdrawStatus.DONE.name
            else -> null
        }
    }

    private fun extractWithdrawOperationId(transferRef: String?): String? {
        if (transferRef == null) return null
        val parts = transferRef.split(":")
        if (parts.size < 3 || parts[0] != "wallet" || parts[1] != "withdraw") return null
        return parts[2]
    }

    private fun withdrawRecordRoleForCategory(category: TransferCategory, eventType: String): String? {
        if (eventType != "WITHDRAW") return null
        return when (category) {
            TransferCategory.WITHDRAW_MANUALLY -> "WITHDRAW_COMPLETION"
            TransferCategory.WITHDRAW_ACCEPT -> "INTERNAL_MOVEMENT"
            else -> null
        }
    }

    private suspend fun createUserTX(command: TransferCommand, txId: Long) {
        val currency = command.amount.currency.symbol
        val amount = command.amount.amount

        when (command.transferCategory) {
            TransferCategory.TRADE -> {
                val loserOwner = command.sourceWallet.owner.id
                val loserMainWallet = walletManager.findWallet(loserOwner!!, currency, WalletType.MAIN) ?: return
                val loserBalance = loserMainWallet.balance

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    loserBalance,
                    -amount,
                    UserTransactionCategory.TRADE
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    gainerBalance + amount,
                    amount,
                    UserTransactionCategory.TRADE,
                )
                userTransactionManager.save(gainerTx)
            }

            TransferCategory.FEE -> {
                val tx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    command.amount.currency.symbol,
                    command.sourceWallet.balance.amount - amount,
                    -amount,
                    UserTransactionCategory.FEE
                )
                userTransactionManager.save(tx)
            }

            TransferCategory.DEPOSIT -> {
                val tx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    command.destWallet.balance.amount + amount,
                    amount,
                    UserTransactionCategory.DEPOSIT,
                    command.description

                )
                userTransactionManager.save(tx)
            }

            TransferCategory.DEPOSIT_MANUALLY -> {
                // TX for user
                val tx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    command.destWallet.balance.amount + amount,
                    amount,
                    UserTransactionCategory.DEPOSIT,
                    command.description
                )
                userTransactionManager.save(tx)

                // TX for admin
                val adminTx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    currency,
                    command.sourceWallet.balance.amount - amount,
                    -amount,
                    UserTransactionCategory.DEPOSIT_TO
                )
                userTransactionManager.save(adminTx)
            }

            TransferCategory.WITHDRAW_MANUALLY -> {
                // TX for user
                val tx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    currency,
                    command.sourceWallet.balance.amount - amount,
                    -amount,
                    UserTransactionCategory.WITHDRAW,
                    command.description
                )
                userTransactionManager.save(tx)

                // TX for admin
                val adminTx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    command.destWallet.balance.amount + amount,
                    amount,
                    UserTransactionCategory.WITHDRAW_FROM
                )
                userTransactionManager.save(adminTx)
            }

            TransferCategory.WITHDRAW_ACCEPT -> {
                val userOwnerId = command.sourceWallet.owner.id!!
                val userWallet = walletManager.findWallet(userOwnerId, currency, WalletType.MAIN) ?: return
                val tx = UserTransaction(
                    userOwnerId,
                    txId,
                    currency,
                    userWallet.balance,
                    -amount,
                    UserTransactionCategory.WITHDRAW
                )
                userTransactionManager.save(tx)
            }

            TransferCategory.PURCHASE_FINALIZED -> {
                val srcTx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    currency,
                    command.sourceWallet.balance.amount - amount,
                    -amount,
                    UserTransactionCategory.SWAP,
                    command.description
                )
                userTransactionManager.save(srcTx)

                val dstTx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    command.destWallet.balance.amount + amount,
                    amount,
                    UserTransactionCategory.SWAP
                )
                userTransactionManager.save(dstTx)
            }
            TransferCategory.IMPERSONATED_PURCHASE_FINALIZED -> {
                val srcTx = UserTransaction(
                    command.sourceWallet.owner.id!!,
                    txId,
                    currency,
                    command.sourceWallet.balance.amount - amount,
                    -amount,
                    UserTransactionCategory.IMPERSONATED_SWAP,
                    command.description
                )
                userTransactionManager.save(srcTx)

                val dstTx = UserTransaction(
                    command.destWallet.owner.id!!,
                    txId,
                    currency,
                    command.destWallet.balance.amount + amount,
                    amount,
                    UserTransactionCategory.IMPERSONATED_SWAP
                )
                userTransactionManager.save(dstTx)
            }

            TransferCategory.KYC_ACCEPTED_REWARD -> {
                val loserOwner = command.sourceWallet.owner.id!!
                val loserMainWallet = command.sourceWallet
                val loserBalance = loserMainWallet.balance.amount

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    loserBalance - amount,
                    -amount,
                    UserTransactionCategory.KYC_ACCEPTED_REWARD
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    gainerBalance + amount,
                    amount,
                    UserTransactionCategory.KYC_ACCEPTED_REWARD,
                )
                userTransactionManager.save(gainerTx)
            }

            TransferCategory.REFERRAL_COMMISSION -> {
                val loserOwner = command.sourceWallet.owner.id!!
                val loserMainWallet = command.sourceWallet
                val loserBalance = loserMainWallet.balance.amount

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    loserBalance - amount,
                    -amount,
                    UserTransactionCategory.REFERRAL_COMMISSION
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    gainerBalance + amount,
                    amount,
                    UserTransactionCategory.REFERRAL_COMMISSION,
                )
                userTransactionManager.save(gainerTx)
            }

            TransferCategory.REFERENT_COMMISSION -> {
                val loserOwner = command.sourceWallet.owner.id!!
                val loserMainWallet = command.sourceWallet
                val loserBalance = loserMainWallet.balance.amount

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    loserBalance - amount,
                    -amount,
                    UserTransactionCategory.REFERENT_COMMISSION
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    gainerBalance + amount,
                    amount,
                    UserTransactionCategory.REFERENT_COMMISSION,
                )
                userTransactionManager.save(gainerTx)
            }

            TransferCategory.REFERRAL_KYC_REWARD -> {
                val loserOwner = command.sourceWallet.owner.id!!
                val loserMainWallet = command.sourceWallet
                val loserBalance = loserMainWallet.balance.amount

                val gainerOwner = command.destWallet.owner.id!!
                val gainerMainWallet = command.destWallet
                val gainerBalance = gainerMainWallet.balance.amount

                val loserTx = UserTransaction(
                    loserOwner,
                    txId,
                    currency,
                    loserBalance - amount,
                    -amount,
                    UserTransactionCategory.REFERRAL_KYC_REWARD
                )
                userTransactionManager.save(loserTx)

                val gainerTx = UserTransaction(
                    gainerOwner,
                    txId,
                    currency,
                    gainerBalance + amount,
                    amount,
                    UserTransactionCategory.REFERRAL_KYC_REWARD,
                )
                userTransactionManager.save(gainerTx)
            }
            else -> {
                // No tx needed for other types
            }
        }
    }
}
