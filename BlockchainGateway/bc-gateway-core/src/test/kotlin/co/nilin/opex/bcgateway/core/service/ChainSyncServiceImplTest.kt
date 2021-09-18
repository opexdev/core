package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxyFinder
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import co.nilin.opex.bcgateway.core.spi.ChainSyncRecordHandler
import co.nilin.opex.bcgateway.core.spi.ChainSyncSchedulerHandler
import co.nilin.opex.bcgateway.core.spi.WalletSyncRecordHandler
import co.nilin.opex.bcgateway.test.OPERATOR
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

internal class ChainSyncServiceImplTest {

    val ethChain = "ETH_MAINNET"
    val bscChain = "BSC_MAINNET"
    val time = LocalDateTime.now()
    val syncService: ChainSyncServiceImpl

    @Mock
    lateinit var chainSyncSchedulerHandler: ChainSyncSchedulerHandler

    @Mock
    lateinit var chainEndpointProxyFinder: ChainEndpointProxyFinder

    @Mock
    lateinit var chainSyncRecordHandler: ChainSyncRecordHandler

    @Mock
    lateinit var walletSyncRecordHandler: WalletSyncRecordHandler

    @Mock
    lateinit var currencyLoader: CurrencyLoader

    val endpointProxy: ChainEndpointProxy = mock()

    init {
        MockitoAnnotations.openMocks(this)
        runBlocking {
            Mockito.`when`(chainEndpointProxyFinder.findChainEndpointProxy(ethChain))
                .thenReturn(endpointProxy)
            Mockito.`when`(currencyLoader.findImplementationsWithTokenOnChain(ethChain)).thenReturn(emptyList())
        }

        syncService = object : ChainSyncServiceImpl(
            chainSyncSchedulerHandler,
            chainEndpointProxyFinder,
            chainSyncRecordHandler,
            walletSyncRecordHandler,
            currencyLoader,
            OPERATOR,
            Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        ) {
            override fun currentTime() = time
        }
    }

    @Test
    fun givenNoActiveSchedules_whenStartSync_thenNoOp() {
        runBlocking {
            //given
            Mockito.`when`(chainSyncSchedulerHandler.fetchActiveSchedules(any())).thenReturn(emptyList())

            //when
            syncService.startSyncWithChain()

            //then
            verifyZeroInteractions(
                chainEndpointProxyFinder,
                chainSyncRecordHandler,
                walletSyncRecordHandler,
                currencyLoader
            )
        }
    }

    @Test
    fun givenAnActiveScheduleAndChainEndpointWorking_whenStartSync_thenSyncedSuccessfully() {
        runBlocking {
            //given
            val delay = 100L
            val syncSchedule = ChainSyncSchedule(ethChain, time, delay)
            Mockito.`when`(chainSyncSchedulerHandler.fetchActiveSchedules(any()))
                .thenReturn(listOf(syncSchedule))
            Mockito.`when`(endpointProxy.syncTransfers(any())).thenReturn(
                ChainSyncRecord(
                    ethChain, LocalDateTime.now(), Endpoint(""), 100, true, null, emptyList()
                )
            )

            //when
            syncService.startSyncWithChain()

            //then
            verify(chainSyncRecordHandler).saveSyncRecord(any())
            verify(walletSyncRecordHandler).saveReadyToSyncTransfers(any(), any())
            verify(chainSyncSchedulerHandler).prepareScheduleForNextTry(syncSchedule, time.plus(delay, ChronoUnit.SECONDS))
        }
    }

    @Test
    fun givenAnActiveScheduleAndChainEndpointFailed_whenStartSync_thenSyncedFailed() {
        runBlocking {
            //given
            val delay = 100L
            val syncSchedule = ChainSyncSchedule(ethChain, time, delay)
            Mockito.`when`(chainSyncSchedulerHandler.fetchActiveSchedules(any()))
                .thenReturn(listOf(syncSchedule))
            Mockito.`when`(endpointProxy.syncTransfers(any())).thenReturn(
                ChainSyncRecord(
                    ethChain, LocalDateTime.now(), Endpoint(""), 100, false, "error", emptyList()
                )
            )

            //when
            syncService.startSyncWithChain()

            //then
            verify(chainSyncRecordHandler).saveSyncRecord(any())
            verify(walletSyncRecordHandler).saveReadyToSyncTransfers(any(), any())
            verify(chainSyncSchedulerHandler, times(0)).prepareScheduleForNextTry(any(), any())
        }
    }


}
