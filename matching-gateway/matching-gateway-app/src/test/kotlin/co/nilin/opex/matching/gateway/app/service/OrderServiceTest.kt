package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.gateway.app.inout.CancelOrderRequest
import co.nilin.opex.matching.gateway.app.inout.CreateOrderRequest
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.app.inout.PairFeeConfig
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stubbing
import java.math.BigDecimal

private class OrderServiceTest : OrderServiceTestBase() {
    @Test
    fun givenLimitASKOrder_whenSubmitNewOrder_thenOrderSubmitResult(): Unit = runBlocking {
        val pairConfig = PairConfig("ETH_USDT", "ETH", "USDT", 0.01, 0.0001)
        stubbing(pairConfigLoader) {
            onBlocking { load("ETH_USDT", OrderDirection.ASK, "") } doReturn PairFeeConfig(
                pairConfig,
                "ASK",
                "",
                0.01,
                0.01
            )
        }
        val order = CreateOrderRequest(
            "a2930d06-0c84-4448-bff7-65134184bb1d",
            "ETH_USDT",
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(0.001),
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )
        stubbing(accountantApiProxy) {
            onBlocking {
                canCreateOrder(order.uuid!!, "ETH", order.quantity)
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

        val orderSubmitResult = orderService.submitNewOrder(order)

        assertThat(orderSubmitResult).isNotNull
    }

    @Test
    fun givenValidCancelOrder_whenCancelOrder_thenOrderSubmitResult(): Unit = runBlocking {
        val order = CancelOrderRequest(
            "edee8090-62d9-4929-b70d-5b97de0c29eb",
            "a2930d06-0c84-4448-bff7-65134184bb1d",
            1,
            "ETH_USDT"
        )
        stubbing(eventSubmitter) {
            onBlocking {
                submit(any())
            } doReturn OrderSubmitResult(null)
        }

        val orderSubmitResult = orderService.cancelOrder(order)

        assertThat(orderSubmitResult).isNotNull
    }
}
