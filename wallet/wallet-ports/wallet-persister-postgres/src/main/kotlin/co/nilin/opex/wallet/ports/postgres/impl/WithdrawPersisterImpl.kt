package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import co.nilin.opex.wallet.ports.postgres.dao.WithdrawRepository
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WithdrawPersisterImpl(private val withdrawRepository: WithdrawRepository) : WithdrawPersister {

    override suspend fun persist(withdraw: Withdraw): Withdraw {
        return withdrawRepository.save(
            WithdrawModel(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                withdraw.finalizedTransaction,
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
        ).awaitFirst().asWithdraw()
    }

    override suspend fun findById(withdrawId: Long): Withdraw? {
        return withdrawRepository.findById(withdrawId)
            .map { it.asWithdraw() }
            .awaitFirstOrNull()
    }

    override suspend fun findWithdrawResponseById(withdrawId: Long): WithdrawResponse? {
        return withdrawRepository.findById(withdrawId)
            .awaitFirstOrNull()
            ?.asWithdrawResponse()
    }

    override suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        offset: Int,
        size: Int
    ): List<WithdrawResponse> {
        return if (status.isEmpty())
            withdrawRepository.findByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
                offset,
                size
            ).map { it.asWithdrawResponse() }.toList()
        else
            withdrawRepository.findByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
                status,
                offset,
                size
            ).map { it.asWithdrawResponse() }.toList()
    }

    override suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>
    ): List<WithdrawResponse> {
        return if (status.isEmpty())
            withdrawRepository.findByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
            ).map { it.asWithdrawResponse() }.toList()
        else
            withdrawRepository.findByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
                status
            ).map { it.asWithdrawResponse() }.toList()
    }

    override suspend fun countByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>
    ): Long {
        return if (status.isEmpty())
            withdrawRepository.countByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
            ).awaitFirstOrElse { 0 }
        else
            withdrawRepository.countByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
                status
            ).awaitFirstOrElse { 0 }
    }

    override suspend fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<WithdrawResponse> {
        val withdraws = if (ascendingByTime == true)
            withdrawRepository.findWithdrawHistoryAsc(uuid, currency, startTime, endTime, limit, offset)
        else
            withdrawRepository.findWithdrawHistoryDesc(uuid, currency, startTime, endTime, limit, offset)
        return withdraws.map { it.asWithdrawResponse() }.toList()
    }

    private suspend fun WithdrawModel.asWithdrawResponse(): WithdrawResponse {
        return WithdrawResponse(
            id!!,
            ownerUuid,
            amount,
            currency,
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

    private fun WithdrawModel.asWithdraw(): Withdraw {
        return Withdraw(
            id,
            ownerUuid,
            currency,
            wallet,
            amount,
            requestTransaction,
            finalizedTransaction,
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