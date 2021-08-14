package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord

interface SyncRecordHandler {
    suspend fun loadLastSuccessRecord(chainName: String): ChainSyncRecord?
    suspend fun saveSyncRecord(syncRecord: ChainSyncRecord)
}