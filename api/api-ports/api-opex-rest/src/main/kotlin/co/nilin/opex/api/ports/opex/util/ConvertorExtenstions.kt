package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.ports.opex.data.OrderDataResponse
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun OrderData.toResponse(): OrderDataResponse {
    return OrderDataResponse(
        symbol = this.symbol,
        orderId = this.orderId,
        orderType = this.orderType,
        side = this.side,
        price = this.price,
        quantity = this.quantity,
        quoteQuantity = this.quoteQuantity,
        executedQuantity = this.executedQuantity,
        takerFee = this.takerFee,
        makerFee = this.makerFee,
        status = OrderStatus.fromCode(this.status) ?: OrderStatus.REJECTED,
        createDate = this.createDate,
        updateDate = this.updateDate,
    )
}


/**
 * LocalDate -> epoch millis (start of day)
 */
fun LocalDate.toTimestamp(
    zoneId: ZoneId = ZoneId.of("UTC")
): Long =
    this
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

/**
 * LocalDateTime -> epoch millis
 */
fun LocalDateTime.toTimestamp(
    zoneId: ZoneId = ZoneId.of("UTC")
): Long =
    this
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

/**
 * epoch millis -> LocalDate
 */
fun Long.toLocalDate(
    zoneId: ZoneId = ZoneId.of("UTC")
): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .toLocalDate()

/**
 * epoch millis -> LocalDateTime
 */
fun Long.toLocalDateTime(
    zoneId: ZoneId = ZoneId.of("UTC")
): LocalDateTime =
    Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .toLocalDateTime()
