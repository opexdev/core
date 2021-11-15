package co.nilin.opex.port.eventlog.postgres.model

import co.nilin.opex.eventlog.spi.Trade
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("opex_trades")
class TradeModel(
    @Id var id: Long?,
    val symbol: String,
    @Column("taker_ouid")
    val takerOuid: String,
    @Column("taker_uuid")
    val takerUuid: String,
    @Column("taker_matching_orderid")
    val takerOrderId: Long,
    @Column("taker_direction")
    val takerDirection: String,
    @Column("taker_price")
    val takerPrice: Long,
    @Column("taker_remained_quantity")
    val takerRemainedQuantity: Long,
    @Column("maker_ouid")
    val makerOuid: String,
    @Column("maker_uuid")
    val makerUuid: String,
    @Column("maker_matching_orderid")
    val makerOrderId: Long,
    @Column("maker_direction")
    val makerDirection: String,
    @Column("maker_price")
    val makerPrice: Long,
    @Column("maker_remained_quantity")
    val makerRemainedQuantity: Long,
    @Column("matched_quantity")
    val matchedQuantity: Long,
    @Column("trade_date")
    val eventDate: LocalDateTime,
    @Column("create_date")
    val createDate: LocalDateTime
) : Trade