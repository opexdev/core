package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.inout.WithdrawAdminResponse
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
import java.util.UUID

@Service
class WithdrawPersisterImpl(private val withdrawRepository: WithdrawRepository) : WithdrawPersister {

    override suspend fun persist(withdraw: Withdraw): Withdraw {
        return withdrawRepository.save(
            WithdrawModel(
                withdraw.withdrawId,
                UUID.randomUUID().toString(),
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
                withdraw.applicator,
                withdraw.withdrawType,
                withdraw.attachment,
                withdraw.createDate,
                withdraw.lastUpdateDate,
                withdraw.transferMethod,
                withdraw.otpRequired
            )
        ).awaitFirst().asWithdraw()
    }


    override suspend fun findByWithdrawUuid(withdrawUuid: String): Withdraw? {
        return withdrawRepository.findByWithdrawUuid(withdrawUuid)
            .map { it.asWithdraw() }
            .awaitFirstOrNull()
    }

    override suspend fun findWithdrawResponseById(withdrawUuid: String): WithdrawResponse? {
        return withdrawRepository.findByWithdrawUuid(withdrawUuid)
            .awaitFirstOrNull()
            ?.asWithdrawResponse()
    }

    override suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean?,
        offset: Int,
        size: Int
    ): List<WithdrawAdminResponse> {
        return if (status.isEmpty())
            withdrawRepository.findByCriteria(
                ownerUuid,
                currency,
                destTxRef,
                destAddress,
                startTime,
                endTime,
                ascendingByTime,
                offset,
                size

            ).toList()
        else
            withdrawRepository.findByCriteria(
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

            ).toList()
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

        val withdraws = withdrawRepository.findWithdrawHistory(
            uuid,
            currency,
            startTime,
            endTime,
            ascendingByTime ?: true,
            limit,
            offset
        )

        return withdraws.map { it.asWithdrawResponse() }.toList()
    }

    override suspend fun findWithdrawHistoryCount(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): Long {

        return withdrawRepository.findWithdrawHistoryCount(
            uuid,
            currency,
            startTime,
            endTime,
        ).awaitFirstOrElse { 0L }

    }


    private suspend fun WithdrawModel.asWithdrawResponse(): WithdrawResponse {
        return WithdrawResponse(
            withdrawUuid!!,
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
            applicator,
            withdrawType,
            attachment,
            createDate,
            lastUpdateDate,
            transferMethod,
            otpRequired
        )
    }

    private fun WithdrawModel.asWithdraw(): Withdraw {
        return Withdraw(
            id,
            withdrawUuid,
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
            applicator,
            withdrawType,
            attachment,
            createDate,
            lastUpdateDate,
            transferMethod,
            otpRequired
        )
    }

    override suspend fun getWithdrawSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): List<TransactionSummary> {
        return withdrawRepository.getWithdrawSummary(uuid, startTime, endTime, limit).toList()
    }
}