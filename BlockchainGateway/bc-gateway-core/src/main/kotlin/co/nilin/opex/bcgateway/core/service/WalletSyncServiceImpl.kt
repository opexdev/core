package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.spi.*
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

class WalletSyncServiceImpl(
    private val syncSchedulerHandler: WalletSyncSchedulerHandler,
    private val walletProxy: WalletProxy,
    private val walletSyncRecordHandler: WalletSyncRecordHandler,
    private val assignedAddressHandler: AssignedAddressHandler,
    private val currencyLoader: CurrencyLoader,
    private val dispatcher: ExecutorCoroutineDispatcher
) : WalletSyncService {

    override suspend fun startSyncWithWallet() {
        withContext(coroutineContext) {
            val schedule = syncSchedulerHandler.fetchActiveSchedule(LocalDateTime.now())
            if (schedule != null) {
                val deposits = walletSyncRecordHandler.findReadyToSyncTransfers(schedule.batchSize)
                deposits.map { deposit ->
                    async(dispatcher) {
                        val uuid = assignedAddressHandler.findUuid(deposit.depositor, deposit.depositorMemo)
                        if ( uuid != null ) {
                            val symbol = currencyLoader.findSymbol(deposit.chain!!, deposit.tokenAddress)
                            walletProxy.transfer(uuid, symbol, deposit.amount)
                        }
                    }
                }
                syncSchedulerHandler.prepareScheduleForNextTry(
                    schedule, LocalDateTime.now()
                        .plus(schedule.delay, ChronoUnit.SECONDS)
                )
            }
        }
    }
}
