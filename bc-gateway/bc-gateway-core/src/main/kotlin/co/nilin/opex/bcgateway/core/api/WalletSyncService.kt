package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.Transfer

interface WalletSyncService {
    suspend fun syncTransfers(transfers: List<Transfer>)
}
