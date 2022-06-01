package co.nilin.opex.bcgateway.core.service

//import co.nilin.opex.bcgateway.core.api.WalletSyncService
//import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
//import co.nilin.opex.bcgateway.core.model.Deposit
//import co.nilin.opex.bcgateway.core.model.WalletSyncRecord
//import co.nilin.opex.bcgateway.core.spi.*
//import kotlinx.coroutines.ExecutorCoroutineDispatcher
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.withContext
//import org.slf4j.LoggerFactory
//import java.math.BigDecimal
//import java.time.LocalDateTime
//import java.time.temporal.ChronoUnit
//import kotlin.coroutines.coroutineContext
//
//class WalletSyncServiceImpl(
//    private val syncSchedulerHandler: WalletSyncSchedulerHandler,
//    private val walletProxy: WalletProxy,
//    private val walletSyncRecordHandler: WalletSyncRecordHandler,
//    private val assignedAddressHandler: AssignedAddressHandler,
//    private val currencyHandler: CurrencyHandler,
//    private val dispatcher: ExecutorCoroutineDispatcher
//) : WalletSyncService {
//
//    private val logger = LoggerFactory.getLogger(ChainSyncServiceImpl::class.java)
//
//    override suspend fun startSyncWithWallet() {
//        withContext(coroutineContext) {
//            val schedule = syncSchedulerHandler.fetchActiveSchedule(LocalDateTime.now())
//            if (schedule != null) {
//                val deposits = walletSyncRecordHandler.findReadyToSyncTransfers(schedule.batchSize)
//                logger.info("syncing ${deposits.size} deposits")
//
//                val result = deposits.map { deposit ->
//                    async(dispatcher) {
//                        var deposited = false
//                        val uuid = assignedAddressHandler.findUuid(
//                            deposit.depositor,
//                            deposit.depositorMemo
//                        )
//                        if (uuid != null) {
//                            logger.info("deposit came for $uuid - to ${deposit.depositor}")
//                            val symbol = currencyHandler.findByChainAndTokenAddress(deposit.chain, deposit.tokenAddress)
//                            if (symbol != null) {
//                                sendDeposit(uuid, symbol, deposit)
//                                deposited = true
//                            }
//                        }
//                        Pair(deposit, deposited)
//                    }
//                }.awaitAll()
//
//                walletSyncRecordHandler.saveWalletSyncRecord(
//                    WalletSyncRecord(
//                        LocalDateTime.now(),
//                        true,
//                        null
//                    ),
//                    result.filter { it.second }.map { it.first },
//                    result.filter { !it.second }.map { it.first }
//                )
//
//                syncSchedulerHandler.prepareScheduleForNextTry(
//                    schedule, LocalDateTime.now()
//                        .plus(schedule.delay, ChronoUnit.SECONDS)
//                )
//            }
//        }
//    }
//
//    private suspend fun sendDeposit(uuid: String, currencyImpl: CurrencyImplementation, deposit: Deposit) {
//        val amount = deposit.amount.divide(BigDecimal(10).pow(currencyImpl.decimal))
//        val symbol = currencyImpl.currency.symbol
//        logger.info("sending deposit to $uuid - $amount $symbol")
//        walletProxy.transfer(uuid, symbol, amount, deposit.hash)
//    }
//}
