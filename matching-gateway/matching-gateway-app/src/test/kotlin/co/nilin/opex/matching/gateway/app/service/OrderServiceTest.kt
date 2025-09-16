package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.service.sample.VALID
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.KafkaHealthIndicator
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.OrderRequestEventSubmitter
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import co.nilin.opex.utility.error.data.OpexException
import io.mockk.MockKException
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private class OrderServiceTest {
    private val accountantApiProxy: AccountantApiProxy = mockk()
    private val orderRequestEventSubmitter: OrderRequestEventSubmitter = mockk()
    private val eventSubmitter: OrderRequestEventSubmitter = mockk()
    private val pairConfigLoader: PairConfigLoader = mockk()
    private val kafkaHealthIndicator: KafkaHealthIndicator = mockk()
    private val pairSettingService: PairSettingService = mockk()

    private val orderService: OrderService = OrderService(
        accountantApiProxy,
        orderRequestEventSubmitter,
        pairConfigLoader,
        pairSettingService,
        kafkaHealthIndicator,
    )

    private fun stubASK() {
        coEvery {
            pairSettingService.load(VALID.ETH_USDT)
        } returns VALID.PAIR_SETTING

        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.ASK
            )
        } returns VALID.PAIR_CONFIG

        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                VALID.ETH,
                VALID.CREATE_ORDER_REQUEST_ASK.quantity
            )
        } returns true

        coEvery {
            orderRequestEventSubmitter.submit(any())
        } returns OrderSubmitResult(null)

        coEvery {
            kafkaHealthIndicator.isHealthy
        } returns true
    }

    private fun stubBID() {
        coEvery {
            pairSettingService.load(VALID.ETH_USDT)
        } returns VALID.PAIR_SETTING
        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.BID
            )
        } returns VALID.PAIR_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                VALID.USDT,
                VALID.CREATE_ORDER_REQUEST_BID.quantity * VALID.CREATE_ORDER_REQUEST_BID.price!!
            )
        } returns true
        coEvery {
            orderRequestEventSubmitter.submit(any())
        } returns OrderSubmitResult(null)
        coEvery {
            kafkaHealthIndicator.isHealthy
        } returns true
    }

    private fun stubBIDWithIOCBudgetMarket() {
        coEvery { pairSettingService.load(VALID.ETH_USDT) } returns VALID.PAIR_SETTING
        coEvery { pairConfigLoader.load(VALID.ETH_USDT, OrderDirection.BID) } returns VALID.PAIR_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET.uuid!!,
                VALID.USDT,
                VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET.totalBudget!!
            )
        } returns true
        coEvery { orderRequestEventSubmitter.submit(any()) } returns OrderSubmitResult(null)
        coEvery { kafkaHealthIndicator.isHealthy } returns true
    }

    private fun stubASKWithIOCBudgetMarket() {
        coEvery { pairSettingService.load(VALID.ETH_USDT) } returns VALID.PAIR_SETTING
        coEvery { pairConfigLoader.load(VALID.ETH_USDT, OrderDirection.ASK) } returns VALID.PAIR_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_MARKET.uuid!!,
                VALID.ETH,
                VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_MARKET.totalBudget!!
            )
        } returns true
        coEvery { orderRequestEventSubmitter.submit(any()) } returns OrderSubmitResult(null)
        coEvery { kafkaHealthIndicator.isHealthy } returns true
    }

    private fun stubASKWithIOCBudgetLimit() {
        coEvery { pairSettingService.load(VALID.ETH_USDT) } returns VALID.PAIR_SETTING
        coEvery { pairConfigLoader.load(VALID.ETH_USDT, OrderDirection.ASK) } returns VALID.PAIR_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_LIMIT.uuid!!,
                VALID.ETH,
                VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_LIMIT.totalBudget!!
            )
        } returns true
        coEvery { orderRequestEventSubmitter.submit(any()) } returns OrderSubmitResult(null)
        coEvery { kafkaHealthIndicator.isHealthy } returns true
    }

    @Test
    fun givenPair_whenSubmitNewOrder_thenOrderSubmitResult(): Unit = runBlocking {
        stubASK()

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByInvalidSymbol_thenThrow(): Unit = runBlocking {
        stubASK()
        clearMocks(pairConfigLoader, pairSettingService)
        coEvery {
            pairConfigLoader.load("BTC_ETH", OrderDirection.ASK)
        } throws Exception()
        coEvery {
            pairSettingService.load("BTC_ETH")
        } throws Exception()
        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(pair = "BTC_ETH"))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKAndInvalidPrice_thenThrow(): Unit = runBlocking {
        stubASK()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(price = BigDecimal.valueOf(-100000)))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKAndInvalidQuantity_thenThrow(): Unit = runBlocking {
        stubASK()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(quantity = BigDecimal.valueOf(-0.001)))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBID_thenOrderSubmitResult(): Unit = runBlocking {
        stubBID()

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidSymbol_thenThrow(): Unit = runBlocking {
        stubBID()
        clearMocks(pairConfigLoader, pairSettingService)
        coEvery {
            pairConfigLoader.load("BTC_USDT", OrderDirection.BID)
        } throws Exception()
        coEvery {
            pairSettingService.load("BTC_USDT")
        } throws Exception()
        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(pair = "BTC_USDT"))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndNotExistOwner_thenThrow(): Unit = runBlocking {
        stubBID()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(uuid = "55408c0a-ed53-42d1-b5ee-b2edf531b9d5"))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidPrice_thenThrow(): Unit = runBlocking {
        stubBID()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(price = BigDecimal.valueOf(-100000)))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidQuantity_thenThrow(): Unit = runBlocking {
        stubBID()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(quantity = BigDecimal.valueOf(-0.001)))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDWithIOCBudgetMarket_thenOrderSubmitResult(): Unit = runBlocking {
        stubBIDWithIOCBudgetMarket()

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDWithIOCBudgetAndInvalidBudget_thenThrow(): Unit = runBlocking {
        stubBIDWithIOCBudgetMarket()

        // Test with budget below minimum
        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET.copy(
                    totalBudget = BigDecimal.valueOf(0.00000001) // Below minimum
                ))
            }
        }.isInstanceOf(OpexException::class.java)
            .hasMessageContaining("Invalid quantity")

        // Test with budget above maximum
        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET.copy(
                    totalBudget = BigDecimal.valueOf(1000000) // Above maximum
                ))
            }
        }.isInstanceOf(OpexException::class.java)
            .hasMessageContaining("Invalid quantity")
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKWithIOCBudgetMarket_thenOrderSubmitResult(): Unit = runBlocking {
        stubASKWithIOCBudgetMarket()

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_MARKET)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKWithIOCBudgetLimit_thenOrderSubmitResult(): Unit = runBlocking {
        stubASKWithIOCBudgetLimit()

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_LIMIT)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDWithIOCBudgetAndPriceSet_thenThrow(): Unit = runBlocking {
        stubBIDWithIOCBudgetMarket()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET.copy(price = BigDecimal.valueOf(3000)))
            }
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKWithIOCBudgetLimitAndNoPrice_thenThrow(): Unit = runBlocking {
        stubASKWithIOCBudgetLimit()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_LIMIT.copy(price = null))
            }
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKWithIOCBudgetMarketAndPriceSet_thenThrow(): Unit = runBlocking {
        stubASKWithIOCBudgetMarket()

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_MARKET.copy(price = BigDecimal.valueOf(3000)))
            }
        }.isInstanceOf(IllegalArgumentException::class.java)
    }




}
