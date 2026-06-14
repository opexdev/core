package co.nilin.opex.market.app.data

import org.apache.kafka.common.protocol.types.Field

data class AdminTradesHistoryRequest(
    val symbol: String?,
    val baseAsset: String?,
    val quoteAsset: String?,
    val uuid: String?,
    val makerUuid: String?,
    val takerUuid: String?,
    val ouid: String?,
    val makerOuid: String?,
    val takerOuid: String?,
    val fromDate: Long?,
    val toDate: Long?,
    val excludeSelfTrade: Boolean = true,
    val ascendingByTime: Boolean = false,
    val limit: Int? = 100,
    val offset: Int? = 0,
)
