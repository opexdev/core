package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.ChainSyncService
import co.nilin.opex.bcgateway.core.spi.*
import kotlinx.coroutines.*
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

open class ChainSyncServiceImpl(
    private val chainSyncSchedulerHandler: ChainSyncSchedulerHandler,
    private val chainEndpointProxyFinder: ChainEndpointProxyFinder,
    private val chainSyncRecordHandler: ChainSyncRecordHandler,
    private val walletSyncRecordHandler: WalletSyncRecordHandler,
    private val currencyLoader: CurrencyLoader,
    private val operator: TransactionalOperator,
    private val dispatcher: ExecutorCoroutineDispatcher
) : ChainSyncService {

    override suspend fun startSyncWithChain() {
        withContext(coroutineContext) {
            val schedules = chainSyncSchedulerHandler.fetchActiveSchedules(currentTime())
            schedules.map { syncSchedule ->
                async(dispatcher) {
                    val syncHandler = chainEndpointProxyFinder.findChainEndpointProxy(syncSchedule.chainName)
                    val lastSync = chainSyncRecordHandler.loadLastSuccessRecord(syncSchedule.chainName)
                    val tokens = currencyLoader.findImplementationsWithTokenOnChain(syncSchedule.chainName)
                        .map { impl -> impl.tokenAddress!! }
                        .toList()
                    val syncResult =
                        syncHandler.syncTransfers(
                            ChainEndpointProxy.DepositFilter(
                                lastSync?.latestBlock, null, tokens
                            )
                        )
                    operator.executeAndAwait {
                        walletSyncRecordHandler.saveReadyToSyncTransfers(syncResult.chainName, syncResult.records)
                        chainSyncRecordHandler.saveSyncRecord(syncResult)
                        if (syncResult.success) {
                            chainSyncSchedulerHandler.prepareScheduleForNextTry(
                                syncSchedule,
                                currentTime().plus(syncSchedule.delay, ChronoUnit.SECONDS)
                            )
                        }
                    }
                }
            }
        }
    }

    protected open fun currentTime() = LocalDateTime.now()
}
