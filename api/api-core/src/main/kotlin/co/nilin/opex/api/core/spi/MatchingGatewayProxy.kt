package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderSubmitResult
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.PairSetting
import java.math.BigDecimal

interface MatchingGatewayProxy {

    suspend fun createNewOrder(
        uuid: String?,
        pair: String,
        price: BigDecimal,
        quantity: BigDecimal,
        direction: OrderDirection,
        matchConstraint: MatchConstraint?,
        orderType: MatchingOrderType,
        userLevel: String,
        token: String?
    ): OrderSubmitResult?

    suspend fun cancelOrder(
        ouid: String,
        uuid: String,
        orderId: Long,
        symbol: String,
        token: String?
    ): OrderSubmitResult?

    suspend fun getPairSettings(): List<PairSetting>
}