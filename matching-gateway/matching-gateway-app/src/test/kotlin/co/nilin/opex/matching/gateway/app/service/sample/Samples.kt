package co.nilin.opex.matching.gateway.app.service.sample

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.gateway.app.inout.CancelOrderRequest
import co.nilin.opex.matching.gateway.app.inout.CreateOrderRequest
import co.nilin.opex.matching.gateway.app.inout.PairConfig
import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import java.math.BigDecimal

object VALID {
    const val UUID = "a2930d06-0c84-4448-bff7-65134184bb1d"

    const val OUID = "edee8090-62d9-4929-b70d-5b97de0c29eb"

    const val ETH = "ETH"

    const val USDT = "USDT"

    const val ETH_USDT = "ETH_USDT"

    const val ORDER_DIRECTION_ASK = "ASK"

    val PAIR_CONFIG = PairConfig(ETH_USDT, ETH, USDT, BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.0001))

    val PAIR_SETTING = PairSetting(ETH_USDT, true, 0.0000001.toBigDecimal(), 100.toBigDecimal(), "LIMIT_ORDER,MARKET_ORDER", null)

    val CREATE_ORDER_REQUEST_ASK = CreateOrderRequest(
        UUID,
        ETH_USDT,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        OrderDirection.ASK,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        "*",
        totalBudget = null
    )

    val CREATE_ORDER_REQUEST_BID = CREATE_ORDER_REQUEST_ASK.copy(direction = OrderDirection.BID)

    val CANCEL_ORDER_REQUEST = CancelOrderRequest(
        OUID,
        UUID,
        1,
        ETH_USDT
    )

    val CREATE_ORDER_REQUEST_BID_IOC_BUDGET_MARKET = CreateOrderRequest(
        uuid = "55408c0a-ed53-42d1-b5ee-b2edf531b9d4",
        pair = "ETH_USDT",
        direction = OrderDirection.BID,
        matchConstraint = MatchConstraint.IOC_BUDGET,
        orderType = OrderType.MARKET_ORDER,
        quantity = BigDecimal.ZERO,
        price = null,
        userLevel = "*",
        totalBudget = BigDecimal.valueOf(100) // Must be between minOrder and maxOrder
    )

    val CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_MARKET = CreateOrderRequest(
        uuid = "55408c0a-ed53-42d1-b5ee-b2edf531b9d4",
        pair = "ETH_USDT",
        direction = OrderDirection.ASK,
        matchConstraint = MatchConstraint.IOC_BUDGET,
        orderType = OrderType.MARKET_ORDER,
        quantity = BigDecimal.ZERO,
        price = null,
        userLevel = "*",
        totalBudget = BigDecimal.valueOf(0.5) // In ETH (base currency)
    )

    val CREATE_ORDER_REQUEST_ASK_IOC_BUDGET_LIMIT = CreateOrderRequest(
        uuid = "55408c0a-ed53-42d1-b5ee-b2edf531b9d4",
        pair = "ETH_USDT",
        direction = OrderDirection.ASK,
        matchConstraint = MatchConstraint.IOC_BUDGET,
        orderType = OrderType.LIMIT_ORDER,
        quantity = BigDecimal.valueOf(2),
        price = BigDecimal.valueOf(500), // Price in USDT per ETH
        userLevel = "*",
        totalBudget = BigDecimal.valueOf(1) // In ETH (base currency)
    )


}
