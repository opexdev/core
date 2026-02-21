package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.*
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

fun Profile.toProfileResponse(): ProfileResponse {
    return ProfileResponse(
        email = this.email,
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        address = this.address,
        mobile = this.mobile,
        telephone = this.telephone,
        postalCode = this.postalCode,
        nationality = this.nationality,
        identifier = this.identifier,
        gender = this.gender,
        birthDate = this.birthDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        status = this.status,
        createDate = this.createDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        lastUpdateDate = this.lastUpdateDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        creator = this.creator,
        kycLevel = this.kycLevel,
        mobileIdentityMatch = this.mobileIdentityMatch,
        personalIdentityMatch = this.personalIdentityMatch
    )
}

fun ProfileApprovalRequestUser.toProfileApprovalRequestUserResponse(): ProfileApprovalRequestUserResponse {
    return ProfileApprovalRequestUserResponse(
        status = this.status,
        createDate = this.createDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        description = this.description,
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
