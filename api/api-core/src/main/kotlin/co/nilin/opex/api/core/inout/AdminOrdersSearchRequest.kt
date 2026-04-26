package co.nilin.opex.api.core.inout

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection

// Admin orders search request (API-only wrapper)
data class AdminOrdersSearchRequest(
    val creatorUuid: String,
    val symbol: String?,
    val startTime: Long?,
    val endTime: Long?,
    val orderType: MatchingOrderType?,
    val direction: OrderDirection?,
    val limit: Int? = 100,
    val offset: Int? = 0,
    val includeNames: Boolean = false,
)
