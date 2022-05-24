package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.service.sample.VALID
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.EventSubmitter
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.KafkaHealthIndicator
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.OrderSubmitter
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private class OrderServiceTest {
    private val accountantApiProxy: AccountantApiProxy = mockk()
    private val orderSubmitter: OrderSubmitter = mockk()
    private val eventSubmitter: EventSubmitter = mockk()
    private val pairConfigLoader: PairConfigLoader = mockk()
    private val kafkaHealthIndicator: KafkaHealthIndicator = mockk()
    private val orderService: OrderService = OrderService(
        accountantApiProxy,
        orderSubmitter,
        eventSubmitter,
        pairConfigLoader,
        kafkaHealthIndicator
    )

    private fun stubASK() {
        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.ASK,
                VALID.USER_LEVEL_REGISTERED
            )
        } returns VALID.PAIR_FEE_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                VALID.ETH,
                VALID.CREATE_ORDER_REQUEST_ASK.quantity
            )
        } returns true
        coEvery {
            orderSubmitter.submit(any())
        } returns OrderSubmitResult(null)
        coEvery {
            kafkaHealthIndicator.isHealthy
        } returns true
    }

    private fun stubBID() {
        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.BID,
                VALID.USER_LEVEL_REGISTERED
            )
        } returns VALID.PAIR_FEE_CONFIG
        coEvery {
            accountantApiProxy.canCreateOrder(
                VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                VALID.USDT,
                VALID.CREATE_ORDER_REQUEST_BID.quantity * VALID.CREATE_ORDER_REQUEST_BID.price
            )
        } returns true
        coEvery {
            orderSubmitter.submit(any())
        } returns OrderSubmitResult(null)
        coEvery {
            kafkaHealthIndicator.isHealthy
        } returns true
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
    fun givenPair_whenSubmitNewOrderByASKAndInvalidLevel_thenThrow(): Unit = runBlocking {
        stubASK()
        clearMocks(pairConfigLoader)
        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.ASK,
                VALID.USER_LEVEL_VERIFIED
            )
        } returns VALID.PAIR_FEE_CONFIG.copy(userLevel = VALID.USER_LEVEL_VERIFIED)

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK)
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
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidLevel_thenThrow(): Unit = runBlocking {
        stubBID()
        clearMocks(pairConfigLoader)
        coEvery {
            pairConfigLoader.load(
                VALID.ETH_USDT,
                OrderDirection.BID,
                VALID.USER_LEVEL_VERIFIED
            )
        } returns VALID.PAIR_FEE_CONFIG.copy(userLevel = VALID.USER_LEVEL_VERIFIED)

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID)
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenEventSubmitter_whenCancelOrder_thenOrderSubmitResult(): Unit = runBlocking {
        coEvery {
            orderSubmitter.submit(any())
        } returns OrderSubmitResult(null)

        val orderSubmitResult = orderService.cancelOrder(VALID.CANCEL_ORDER_REQUEST)

        assertThat(orderSubmitResult).isNotNull
    }
}
