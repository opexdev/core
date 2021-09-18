package co.nilin.opex.bcgateway.core.api

interface WalletSyncService {
    suspend fun startSyncWithWallet()
}