package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.SyncRecordHandler
import co.nilin.opex.port.bcgateway.postgres.dao.ChainSyncRecordRepository
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class SyncRecordHandlerImpl(private val chainSyncRecordRepository: ChainSyncRecordRepository) : SyncRecordHandler {
    override suspend fun loadLastSuccessRecord(chainName: String): ChainSyncRecord? {
        val dao = chainSyncRecordRepository.findByChain(chainName).awaitSingleOrNull()
        return if (dao !== null) {
            ChainSyncRecord(
                dao.chain,
                dao.time,
                Endpoint(dao.endpointUrl),
                dao.latestBlock,
                dao.success,
                dao.error,
                emptyList()
            )
        } else {
            null
        }
    }

    override suspend fun saveSyncRecord(syncRecord: ChainSyncRecord) {
        TODO("Not yet implemented")
    }
}