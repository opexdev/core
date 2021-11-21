package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.WithdrawRepository
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
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
        status: List<String>?
    ): List<WithdrawResponse> {
        return withdrawRepository
            .findByCriteria(ownerUuid, withdrawId?.toLong(), currency, destTxRef, destAddress, noStatus, status)
            .map { wm ->
                val reqTx = transactionRepository.findById(wm.requestTransaction.toLong()).awaitFirst()
                val finalTx = if (wm.finalizedTransaction == null)
                    null
                else
                    transactionRepository.findById(wm.finalizedTransaction.toLong()).awaitFirstOrNull()
                WithdrawResponse(
                    wm.id!!,
                    wm.ownerUuid,
                    Date.from(
                        reqTx.txDate.atZone(ZoneId.systemDefault()).toInstant()
                    ),
                    if (finalTx == null)
                        null
                    else
                        Date.from(
                            finalTx.txDate.atZone(ZoneId.systemDefault()).toInstant()
                        ),
                    reqTx.id.toString(),
                    finalTx?.id.toString(),
                    wm.acceptedFee,
                    wm.appliedFee,
                    wm.amount,
                    wm.destAmount,
                    wm.destCurrency,
                    wm.destAddress,
                    wm.destNetwork,
                    wm.destNote,
                    wm.destTransactionRef,
                    wm.statusReason,
                    wm.status,
                    wm.createDate,
                    wm.acceptDate
                )
            }
            .toList()
    }

    override suspend fun persist(withdraw: Withdraw): Withdraw {
        val wm = withdrawRepository.save(
            WithdrawModel(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                withdraw.finalizedTransaction,
                withdraw.acceptedFee,
                withdraw.appliedFee,
                withdraw.destAmount,
                withdraw.destCurrency,
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
        return Withdraw(
            wm.id,
            withdraw.ownerUuid,
            withdraw.wallet,
            withdraw.amount,
            withdraw.requestTransaction,
            withdraw.finalizedTransaction,
            withdraw.acceptedFee,
            withdraw.appliedFee,
            withdraw.destAmount,
            withdraw.destCurrency,
            withdraw.destAddress,
            withdraw.destNetwork,
            withdraw.destNote,
            withdraw.destTransactionRef,
            withdraw.statusReason,
            withdraw.status,
            withdraw.createDate,
            withdraw.acceptDate
        )
    }

    override suspend fun findById(withdrawId: String): Withdraw? {
        return withdrawRepository.findById(withdrawId)
            .map { withdraw ->
                Withdraw(
                    withdraw.id,
                    withdraw.ownerUuid,
                    withdraw.wallet,
                    withdraw.amount,
                    withdraw.requestTransaction,
                    withdraw.finalizedTransaction,
                    withdraw.acceptedFee,
                    withdraw.appliedFee,
                    withdraw.destAmount,
                    withdraw.destCurrency,
                    withdraw.destAddress,
                    withdraw.destNetwork,
                    withdraw.destNote,
                    withdraw.destTransactionRef,
                    withdraw.statusReason,
                    withdraw.status,
                    withdraw.createDate,
                    withdraw.acceptDate
                )
            }
            .awaitFirstOrNull()
    }

    override suspend fun findWithdrawHistory(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int,
        offset: Int
    ): List<Withdraw> {
        val withdraws = if (coin == null)
            withdrawRepository.findWithdrawHistory(uuid, startTime, endTime, limit)
        else
            withdrawRepository.findWithdrawHistory(uuid, coin, startTime, endTime, limit)

        return withdraws.map { withdraw ->
            Withdraw(
                withdraw.id,
                withdraw.ownerUuid,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                withdraw.finalizedTransaction,
                withdraw.acceptedFee,
                withdraw.appliedFee,
                withdraw.destAmount,
                withdraw.destCurrency,
                withdraw.destAddress,
                withdraw.destNetwork,
                withdraw.destNote,
                withdraw.destTransactionRef,
                withdraw.statusReason,
                withdraw.status,
                withdraw.createDate,
                withdraw.acceptDate
            )
        }.toList()
    }
}