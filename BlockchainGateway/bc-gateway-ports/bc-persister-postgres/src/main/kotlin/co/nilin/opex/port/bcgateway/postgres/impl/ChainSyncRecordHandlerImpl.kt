package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainSyncRecordHandler
import co.nilin.opex.port.bcgateway.postgres.dao.ChainSyncRecordRepository
import co.nilin.opex.port.bcgateway.postgres.dao.ChainSyncDepositRepository
import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncDepositModel
import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncRecordModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ChainSyncRecordHandlerImpl(
    private val chainSyncRecordRepository: ChainSyncRecordRepository,
    private val chainSyncDepositRepository: ChainSyncDepositRepository
) : ChainSyncRecordHandler {
    override suspend fun loadLastSuccessRecord(chainName: String): ChainSyncRecord? {
        val chainSyncRecordDao = chainSyncRecordRepository.findByChain(chainName).awaitSingleOrNull()
        return if (chainSyncRecordDao !== null) {
            val deposits = chainSyncDepositRepository.findByChain(chainName).map {
                Deposit(it.depositor, it.depositorMemo, it.amount, it.chain, it.token, it.tokenAddress)
            }
            ChainSyncRecord(
                chainSyncRecordDao.chain,
                chainSyncRecordDao.time,
                Endpoint(chainSyncRecordDao.endpointUrl),
                chainSyncRecordDao.latestBlock,
                chainSyncRecordDao.success,
                chainSyncRecordDao.error,
                deposits.toList()
            )
        } else {
            null
        }
    }

    @Transactional
    override suspend fun saveSyncRecord(syncRecord: ChainSyncRecord) {
        val chainSyncRecordDao =
            ChainSyncRecordModel(
                syncRecord.chainName,
                syncRecord.time,
                syncRecord.endpoint.url,
                syncRecord.latestBlock,
                syncRecord.success,
                syncRecord.error
            )
        chainSyncRecordRepository.save(chainSyncRecordDao).awaitFirst()
        val depositsDao = syncRecord.records.map {
            ChainSyncDepositModel(
                null,
                it.depositor,
                it.depositorMemo,
                it.amount,
                it.chain,
                it.token,
                it.tokenAddress
            )
        }
        chainSyncDepositRepository.saveAll(depositsDao).awaitFirst()
    }
}
