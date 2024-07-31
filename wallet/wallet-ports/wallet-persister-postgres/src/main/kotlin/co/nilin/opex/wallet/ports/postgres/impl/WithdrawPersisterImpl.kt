package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.WithdrawRepository
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class WithdrawPersisterImpl(
    private val withdrawRepository: WithdrawRepository,
    private val transactionRepository: TransactionRepository
) : WithdrawPersister {

    override suspend fun findByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?,
        offset: Int,
        size: Int
    ): List<WithdrawResponse> {
        return withdrawRepository
            .findByCriteria(
                ownerUuid,
                withdrawId?.toLong(),
                currency,
                destTxRef,
                destAddress,
                noStatus,
                status,
                offset,
                size
            )
            .map { it.asWithdrawResponse() }
            .toList()
    }

    override suspend fun countByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?
    ): Long {
        return withdrawRepository.countByCriteria(
            ownerUuid,
            withdrawId?.toLong(),
            currency,
            destTxRef,
            destAddress,
            noStatus,
            status
        ).awaitFirstOrElse { 0 }
    }

    override suspend fun findByCriteria(
        ownerUuid: String?,
        withdrawId: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        noStatus: Boolean,
        status: List<WithdrawStatus>?
    ): List<WithdrawResponse> {
        return withdrawRepository
            .findByCriteria(
                ownerUuid,
                withdrawId?.toLong(),
                currency,
                destTxRef,
                destAddress,
                noStatus,
                status
            )
            .map { it.asWithdrawResponse() }
            .toList()
    }

    override suspend fun persist(withdraw: Withdraw): Withdraw {
        val wm = withdrawRepository.save(
            WithdrawModel(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                withdraw.finalizedTransaction,
                withdraw.acceptedFee,
                withdraw.appliedFee,
                withdraw.destAmount,
                withdraw.destSymbol,
                withdraw.destNetwork,
                withdraw.destAddress,
                withdraw.destNote,
                withdraw.destTransactionRef,
                withdraw.statusReason,
                withdraw.status,
                withdraw.createDate,
                withdraw.acceptDate
            )
        ).awaitFirst()

        return wm.asWithdraw()
    }

    override suspend fun findById(withdrawId: String): Withdraw? {
        return withdrawRepository.findById(withdrawId)
            .map { it.asWithdraw() }
            .awaitFirstOrNull()
    }

    override suspend fun findWithdrawHistory(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<Withdraw> {
        val withdraws = if (ascendingByTime == true)
            withdrawRepository.findWithdrawHistoryAsc(uuid, coin, startTime, endTime, limit, offset)
        else
            withdrawRepository.findWithdrawHistoryDesc(uuid, coin, startTime, endTime, limit, offset)
        return withdraws.map { it.asWithdraw() }.toList()
    }


    private suspend fun WithdrawModel.asWithdrawResponse(): WithdrawResponse {
        val reqTx = transactionRepository.findById(requestTransaction.toLong()).awaitFirst()
        val finalTx = if (finalizedTransaction == null)
            null
        else
            transactionRepository.findById(finalizedTransaction.toLong()).awaitFirstOrNull()

        return WithdrawResponse(
            id!!,
            ownerUuid,
            Date.from(reqTx.txDate.atZone(ZoneId.systemDefault()).toInstant()),
            if (finalTx == null) null else Date.from(finalTx.txDate.atZone(ZoneId.systemDefault()).toInstant()),
            reqTx.id.toString(),
            finalTx?.id.toString(),
            acceptedFee,
            appliedFee,
            amount,
            destAmount,
            destSymbol,
            destAddress,
            destNetwork,
            destNotes,
            destTransactionRef,
            statusReason,
            status,
            createDate,
            acceptDate
        )
    }

    private fun WithdrawModel.asWithdraw(): Withdraw {
        return Withdraw(
            id,
            ownerUuid,
            currency,
            wallet,
            amount,
            requestTransaction,
            finalizedTransaction,
            acceptedFee,
            appliedFee,
            destAmount,
            destSymbol,
            destAddress,
            destNetwork,
            destNotes,
            destTransactionRef,
            statusReason,
            status,
            createDate,
            acceptDate
        )
    }
}