package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column

data class TradeTickerData(
    val symbol: String,
    @Column("price_change")
    val priceChange: Double?,
    @Column("price_change_percent")
    val priceChangePercent: Double?,
    @Column("weighted_avg_price")
    val weightedAvgPrice: Double?,
    @Column("last_price")
    val lastPrice: Double?,
    @Column("last_qty")
    val lastQty: Double?,
    @Column("bid_price")
    val bidPrice: Double?,
    @Column("ask_price")
    val askPrice: Double?,
    @Column("open_price")
    val openPrice: Double?,
    @Column("high_price")
    val highPrice: Double?,
    @Column("low_price")
    val lowPrice: Double?,
    val volume: Double?,
    @Column("first_id")
    val firstId: Long?,
    @Column("last_id")
    val lastId: Long?,
    val count: Long?,
)
