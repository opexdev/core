package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.common.utils.CacheManager
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


internal class FeeCalculatorImplTest {
    private val walletProxy = mockk<WalletProxy>()
    private val feeConfigService = mockk<FeeConfigService>()
    private val userVolumePersister = mockk<UserVolumePersister>()
    @Qualifier("appCacheManager") private val cacheManager = mockk<CacheManager<String, UserFee>>()


    private val receiverAddress = "0x0"
    private val feeCalculator = FeeCalculatorImpl(
        walletProxy,
        feeConfigService,
        userVolumePersister,
        cacheManager,
        receiverAddress,
        "+03:30",
        "USDT",
        JsonMapperTestImpl(),
    )

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_feeActionsNotNull(): Unit = runBlocking {
        val actions =
            feeCalculator.createFeeActions(Valid.tradeEvent, Valid.makerOrder, Valid.takerOrder, null, null)
        assertThat(actions.makerFeeAction).isNotNull
        assertThat(actions.takerFeeAction).isNotNull
    }

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_returnCorrectFees(): Unit = runBlocking {
        val actions =
            feeCalculator.createFeeActions(Valid.tradeEvent, Valid.makerOrder, Valid.takerOrder, null, null)
        with(actions.makerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(0.01) // 1% of 1 BTC
            assertThat(symbol).isEqualTo("BTC")
            assertThat(sender).isEqualTo("user_1")
            assertThat(pointer).isEqualTo("order_2")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo(WalletType.EXCHANGE)
        }

        with(actions.takerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(500.0) // 1% of 50,000 USDT
            assertThat(symbol).isEqualTo("USDT")
            assertThat(sender).isEqualTo("user_2")
            assertThat(pointer).isEqualTo("order_1")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo(WalletType.EXCHANGE)
        }
    }

    @Test
    fun givenTradeEventsAndOrders_whenFAParentNotNull_thenFeeActionParentNotNull(): Unit = runBlocking {
        val parentFA = FinancialAction(
            null,
            TradeEvent::class.java.name,
            "trade_id",
            "BTC_USDT",
            10000.0.toBigDecimal(),
            "user_parent",
            WalletType.MAIN,
            "system",
            WalletType.MAIN,
            Valid.currentTime,
            FinancialActionCategory.TRADE
        )

        val actions = feeCalculator.createFeeActions(
            Valid.tradeEvent,
            Valid.makerOrder,
            Valid.takerOrder,
            parentFA,
            parentFA
        )
        assertThat(actions.makerFeeAction.parent).isNotNull
        assertThat(actions.takerFeeAction.parent).isNotNull
    }

    @Test
    fun givenCachedValueExists_whenGetUserFeeCalled_thenReturnCachedValueAndSkipServices() {
        runBlocking {
            val uuid = "user_1"
            val cachedFee = UserFee("test1", 1, BigDecimal("0.1"), BigDecimal("0.1"))

            every { cacheManager.get(uuid) } returns cachedFee

            val result = feeCalculator.getUserFee(uuid)

            assertThat(result).isEqualTo(cachedFee)
            verify(exactly = 1) { cacheManager.get(uuid) }
            verify { walletProxy wasNot Called }
            verify { userVolumePersister wasNot Called }
            verify { feeConfigService wasNot Called }
        }
    }

    @Test
    fun givenNoCachedValue_whenGetUserFee_thenFetchFromServicesAndCacheIt() = runBlocking {
        val uuid = "user_2"
        val expectedFee = UserFee("test2", 2, BigDecimal("0.2"), BigDecimal("0.2"))

        every { cacheManager.get(uuid) } returns null
        coEvery { walletProxy.getUserTotalAssets(uuid) } returns mockk {
            every { totalAmount } returns BigDecimal("1000")
        }
        coEvery { userVolumePersister.getUserTotalTradeVolume(any(), any(), any()) } returns BigDecimal("200")
        coEvery { feeConfigService.loadMatchingFeeConfig(any(), any()) } returns expectedFee
        every { cacheManager.put(eq(uuid), eq(expectedFee), any(), eq(TimeUnit.MILLISECONDS)) } just Runs

        val result = feeCalculator.getUserFee(uuid)

        assertThat(result).isEqualTo(expectedFee)
        coVerifyOrder {
            cacheManager.get(uuid)
            walletProxy.getUserTotalAssets(uuid)
        }
        coVerifyOrder {
            userVolumePersister.getUserTotalTradeVolume(any(), any(), any())
        }
        coVerifyOrder {
            feeConfigService.loadMatchingFeeConfig(BigDecimal("1000"), BigDecimal("200"))
            cacheManager.put(uuid, expectedFee, any(), TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun givenAssetsAndVolume_whenGetUserFee_thenPassCorrectValuesToFeeConfigService() = runBlocking {
        val uuid = "user_3"
        val assets = BigDecimal("5000")
        val volume = BigDecimal("300")
        val expectedFee = UserFee("test", 1, BigDecimal("0.1"), BigDecimal("0.1"))

        every { cacheManager.get(uuid) } returns null
        coEvery { walletProxy.getUserTotalAssets(uuid) } returns mockk { every { totalAmount } returns assets }
        coEvery { userVolumePersister.getUserTotalTradeVolume(eq(uuid), any(), eq("USDT")) } returns volume
        coEvery { feeConfigService.loadMatchingFeeConfig(assets, volume) } returns expectedFee
        every { cacheManager.put(eq(uuid), eq(expectedFee), any(), eq(TimeUnit.MILLISECONDS)) } just Runs

        val result = feeCalculator.getUserFee(uuid)

        assertThat(result).isEqualTo(expectedFee)
        coVerify { feeConfigService.loadMatchingFeeConfig(assets, volume) }
    }

}