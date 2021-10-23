package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.WalletSyncService
import co.nilin.opex.bcgateway.core.model.WalletSyncRecord
import co.nilin.opex.bcgateway.core.spi.*
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(ChainSyncServiceImpl::class.java)

    override suspend fun startSyncWithWallet() {
        withContext(coroutineContext) {
            val schedule = syncSchedulerHandler.fetchActiveSchedule(LocalDateTime.now())
            if (schedule != null) {
                val deposits = walletSyncRecordHandler.findReadyToSyncTransfers(schedule.batchSize)
                logger.info("syncing ${deposits.size} deposits")
                deposits.map { deposit ->
                    async(dispatcher) {
                        val uuid = assignedAddressHandler.findUuid(deposit.depositor, deposit.depositorMemo)
                        if (uuid != null) {
                            logger.info("deposit came for $uuid - to ${deposit.depositor}")
                            val symbol = currencyLoader.findSymbol(deposit.chain, deposit.tokenAddress)
                            if (symbol != null) {
                                logger.info("sending deposit to $uuid - ${deposit.amount} $symbol")
                                walletProxy.transfer(uuid, symbol, deposit.amount, deposit.hash)
                            }
                        }
                        walletSyncRecordHandler.saveWalletSyncRecord(
                            WalletSyncRecord(
                                LocalDateTime.now(),
                                true,
                                null,
                                deposit
                            )
                        )
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
