package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import co.nilin.opex.bcgateway.core.spi.SyncRecordHandler
import co.nilin.opex.bcgateway.core.spi.SyncSchedulerHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SyncRecordHandlerImpl : SyncRecordHandler {
    override suspend fun loadLastSuccessRecord(chainName: String): ChainSyncRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun saveSyncRecord(syncRecord: ChainSyncRecord) {
        TODO("Not yet implemented")
    }
}