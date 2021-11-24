package co.nilin.opex.bcgateway.core.api

interface ChainSyncService {
    suspend fun startSyncWithChain()
}