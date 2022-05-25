package co.nilin.opex.websocket.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal

data class TradeTickerData(
    val symbol: String,
    @Column("price_change")
    val priceChange: BigDecimal?,
    @Column("price_change_percent")
    val priceChangePercent: BigDecimal?,
    @Column("weighted_avg_price")
    val weightedAvgPrice: BigDecimal?,
    @Column("last_price")
    val lastPrice: BigDecimal?,
    @Column("last_qty")
    val lastQty: BigDecimal?,
    @Column("bid_price")
    val bidPrice: BigDecimal?,
    @Column("ask_price")
    val askPrice: BigDecimal?,
    @Column("open_price")
    val openPrice: BigDecimal?,
    @Column("high_price")
    val highPrice: BigDecimal?,
    @Column("low_price")
    val lowPrice: BigDecimal?,
    val volume: BigDecimal?,
    @Column("first_id")
    val firstId: Long?,
    @Column("last_id")
    val lastId: Long?,
    val count: Long?,
)
