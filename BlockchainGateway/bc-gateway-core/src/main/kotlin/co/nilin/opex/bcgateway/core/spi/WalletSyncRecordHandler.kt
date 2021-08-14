package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.Deposit

interface WalletSyncRecordHandler {
    suspend fun saveReadyToSyncTransfers(chainName: String, deposits: List<Deposit>)
    suspend fun findReadyToSyncTransfers(count: Long?): List<Deposit>
}