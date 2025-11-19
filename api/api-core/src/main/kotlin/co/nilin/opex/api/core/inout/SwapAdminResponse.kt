package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class SwapAdminResponse(
    var reserveNumber: String,
    var sourceSymbol: String,
    var destSymbol: String,
    var uuid: String,
    var ownerName: String?,
    var sourceAmount: BigDecimal,
    var reservedDestAmount: BigDecimal,
    var reserveDate: LocalDateTime? = LocalDateTime.now(),
    var expDate: LocalDateTime? = null,
    var status: ReservedStatus? = null,
    val rate: BigDecimal? = null,
)
