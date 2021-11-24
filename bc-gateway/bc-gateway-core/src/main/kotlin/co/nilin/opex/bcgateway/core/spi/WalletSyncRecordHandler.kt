package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.WalletSyncRecord

interface WalletSyncRecordHandler {

    suspend fun saveReadyToSyncTransfers(chainName: String, deposits: List<Deposit>)

    suspend fun saveWalletSyncRecord(
        syncRecord: WalletSyncRecord,
        sentDeposits: List<Deposit>,
        deletingDeposits: List<Deposit>
    )

    suspend fun findReadyToSyncTransfers(count: Long?): List<Deposit>
}
