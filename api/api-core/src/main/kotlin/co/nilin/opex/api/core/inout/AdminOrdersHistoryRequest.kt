package co.nilin.opex.api.core.inout

data class AdminOrdersHistoryRequest(
    val uuid: String?,
    val symbol: String?,
    val ouid: String?,
    val startTime: Long?,
    val endTime: Long?,
    val orderType: MatchingOrderType?,
    val direction: OrderDirection?,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean? = false,
    val includeNames: Boolean? = false
)
