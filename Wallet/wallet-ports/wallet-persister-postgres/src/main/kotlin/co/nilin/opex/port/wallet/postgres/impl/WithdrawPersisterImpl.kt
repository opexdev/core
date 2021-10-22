package co.nilin.opex.port.wallet.postgres.impl

import co.nilin.opex.port.wallet.postgres.dao.TransactionRepository
import co.nilin.opex.port.wallet.postgres.dao.WithdrawRepository
import co.nilin.opex.port.wallet.postgres.model.WithdrawModel
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

@Service
class WithdrawPersisterImpl(
    val withdrawRepository: WithdrawRepository, val transactionRepository: TransactionRepository
) : WithdrawPersister {
    override suspend fun findByCriteria(ownerUuid: String?, withdrawId: String?, currency: String?, destTxRef: String?, destAddress: String?, noStatus: Boolean, status: List<String>?): List<WithdrawResponse> {
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
                    wm.ownerUuid, Date.from(
                        reqTx.txDate.atZone(ZoneId.systemDefault()).toInstant()
                    ), if (finalTx == null)
                        null
                    else
                        Date.from(
                            finalTx.txDate.atZone(ZoneId.systemDefault()).toInstant()
                        ), reqTx.id.toString(), finalTx?.id.toString(), wm.acceptedFee, wm.appliedFee, wm.amount, wm.destAmount, wm.destCurrency, wm.destAddress, wm.destNetwork, wm.destNote, wm.destTransactionRef, wm.statusReason, wm.status
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
                withdraw.status
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
            withdraw.destNetwork,
            withdraw.destAddress,
            withdraw.destNote,
            withdraw.destTransactionRef,
            withdraw.statusReason,
            withdraw.status
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
                    withdraw.destNetwork,
                    withdraw.destAddress,
                    withdraw.destNote,
                    withdraw.destTransactionRef,
                    withdraw.statusReason,
                    withdraw.status
                )
            }
            .awaitFirstOrNull()
    }
}