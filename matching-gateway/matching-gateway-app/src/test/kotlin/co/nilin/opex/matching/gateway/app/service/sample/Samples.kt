package co.nilin.opex.matching.gateway.app.service.sample

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.gateway.app.inout.CancelOrderRequest
import co.nilin.opex.matching.gateway.app.inout.CreateOrderRequest
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import java.math.BigDecimal

object VALID {
    const val UUID = "a2930d06-0c84-4448-bff7-65134184bb1d"

    const val OUID = "edee8090-62d9-4929-b70d-5b97de0c29eb"

    const val ETH = "ETH"

    const val USDT = "USDT"

    const val ETH_USDT = "ETH_USDT"

    const val ORDER_DIRECTION_ASK = "ASK"

    val PAIR_CONFIG = PairConfig(ETH_USDT, ETH, USDT, BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.0001))

    val CREATE_ORDER_REQUEST_ASK = CreateOrderRequest(
        UUID,
        ETH_USDT,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        OrderDirection.ASK,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        "*"
    )

    val CREATE_ORDER_REQUEST_BID = CREATE_ORDER_REQUEST_ASK.copy(direction = OrderDirection.BID)

    val CANCEL_ORDER_REQUEST = CancelOrderRequest(
        OUID,
        UUID,
        1,
        ETH_USDT
    )
}
