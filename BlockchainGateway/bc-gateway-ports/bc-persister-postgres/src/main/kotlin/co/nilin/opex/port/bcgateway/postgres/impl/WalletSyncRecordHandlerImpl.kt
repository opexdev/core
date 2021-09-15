package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.WalletSyncRecord
import co.nilin.opex.bcgateway.core.spi.WalletSyncRecordHandler
import co.nilin.opex.port.bcgateway.postgres.dao.DepositRepository
import co.nilin.opex.port.bcgateway.postgres.dao.WalletSyncRecordRepository
import co.nilin.opex.port.bcgateway.postgres.model.DepositModel
import co.nilin.opex.port.bcgateway.postgres.model.WalletSyncRecordModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WalletSyncRecordHandlerImpl(
    private val walletSyncRecordRepository: WalletSyncRecordRepository,
    private val depositRepository: DepositRepository
) : WalletSyncRecordHandler {
    @Transactional
    override suspend fun saveReadyToSyncTransfers(chainName: String, deposits: List<Deposit>) {
        val depositsDao = deposits.map {
            DepositModel(
                null,
                null,
                it.depositor,
                it.depositorMemo,
                it.amount,
                it.chain,
                it.token,
                it.tokenAddress
            )
        }
        depositRepository.saveAll(depositsDao).awaitFirst()
    }

    @Transactional
    override suspend fun saveWalletSyncRecord(syncRecord: WalletSyncRecord) {
        val dao = walletSyncRecordRepository.save(
            WalletSyncRecordModel(
                null,
                syncRecord.time,
                syncRecord.success,
                syncRecord.error
            )
        ).awaitFirst()
        depositRepository.updateWalletSyncRecord(syncRecord.deposit.id!!, dao.id!!).awaitFirst()
    }

    override suspend fun findReadyToSyncTransfers(count: Long?): List<Deposit> {
        return depositRepository.findLimited(count).map {
            Deposit(it.id, it.depositor, it.depositorMemo, it.amount, it.chain, it.token, it.tokenAddress)
        }.toList()
    }
}
