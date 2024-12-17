package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.Transfer

interface WalletSyncService {

    suspend fun sendTransfer(transfer: Transfer)

    @Deprecated("Use above function instead")
    suspend fun syncTransfers(transfers: List<Transfer>)
}
