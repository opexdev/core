package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.gateway.app.service.sample.VALID
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.EventSubmitter
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.KafkaHealthIndicator
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.OrderSubmitter
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import java.math.BigDecimal

private class OrderServiceTest {
    private val accountantApiProxy: AccountantApiProxy = mock()
    private val orderSubmitter: OrderSubmitter = mock()
    private val eventSubmitter: EventSubmitter = mock()
    private val pairConfigLoader: PairConfigLoader = mock()
    private val kafkaHealthIndicator: KafkaHealthIndicator = mock()
    private val orderService: OrderService = OrderService(
        accountantApiProxy,
        orderSubmitter,
        eventSubmitter,
        pairConfigLoader,
        kafkaHealthIndicator
    )

    @Test
    fun givenPair_whenSubmitNewOrder_thenOrderSubmitResult(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.ASK,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_ASK.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByInvalidSymbol_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.ASK,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_ASK.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(pair = "BTC_ETH"))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKAndInvalidPrice_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.ASK,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_ASK.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(price = BigDecimal.valueOf(-100000)))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKAndInvalidQuantity_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.ASK,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_ASK.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK.copy(quantity = BigDecimal.valueOf(-0.001)))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByASKAndInvalidLevel_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.ASK,
                    "verified"
                )
            } doReturn VALID.PAIR_FEE_CONFIG.copy(userLevel = "verified")
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_ASK.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_ASK.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_ASK)
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBID_thenOrderSubmitResult(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        val orderSubmitResult = orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidSymbol_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(pair = "BTC_USDT"))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndNotExistOwner_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(uuid = "55408c0a-ed53-42d1-b5ee-b2edf531b9d5"))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidPrice_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(price = BigDecimal.valueOf(-100000)))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidQuantity_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    VALID.USER_LEVEL_REGISTERED
                )
            } doReturn VALID.PAIR_FEE_CONFIG
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy {
            runBlocking {
                orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID.copy(quantity = BigDecimal.valueOf(-0.001)))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenPair_whenSubmitNewOrderByBIDAndInvalidLevel_thenThrow(): Unit = runBlocking {
        stubbing(pairConfigLoader) {
            onBlocking {
                load(
                    VALID.ETH_USDT,
                    OrderDirection.BID,
                    "verified"
                )
            } doReturn VALID.PAIR_FEE_CONFIG.copy(userLevel = "verified")
        }
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(
                    VALID.CREATE_ORDER_REQUEST_BID.uuid!!,
                    VALID.ETH,
                    VALID.CREATE_ORDER_REQUEST_BID.quantity
                )
            } doReturn true
        }
        stubbing(orderSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }
        stubbing(kafkaHealthIndicator) {
            on { isHealthy } doReturn true
        }

        assertThatThrownBy { runBlocking { orderService.submitNewOrder(VALID.CREATE_ORDER_REQUEST_BID) } }.isNotInstanceOf(
            NullPointerException::class.java
        )
    }

    @Test
    fun givenEventSubmitter_whenCancelOrder_thenOrderSubmitResult(): Unit = runBlocking {
        stubbing(eventSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }

        val orderSubmitResult = orderService.cancelOrder(VALID.CANCEL_ORDER_REQUEST)

        assertThat(orderSubmitResult).isNotNull
    }
}
