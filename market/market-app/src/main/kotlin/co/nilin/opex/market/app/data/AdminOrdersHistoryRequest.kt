package co.nilin.opex.market.app.data

import co.nilin.opex.market.core.inout.MatchingOrderType
import co.nilin.opex.market.core.inout.OrderDirection

data class AdminOrdersHistoryRequest(
    val uuid: String?,
    val symbol: String?,
    val ouid: String?,
    val startTime: Long?,
    val endTime: Long?,
    val orderType: MatchingOrderType?,
    val direction: OrderDirection?,
    val limit: Int? = 100,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false,
    )
