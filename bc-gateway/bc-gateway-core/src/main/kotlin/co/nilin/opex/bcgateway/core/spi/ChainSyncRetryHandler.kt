package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule

interface ChainSyncRetryHandler {

    suspend fun handleNextTry(syncSchedule: ChainSyncSchedule, records: ChainSyncRecord, sentBlock:Long)

}