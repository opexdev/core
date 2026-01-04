package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class ReservedTransferResponse(
    var reserveNumber: String,
    var sourceSymbol: String,
    var destSymbol: String,
    var receiverUuid: String,
    var sourceAmount: BigDecimal,
    var guaranteedDestAmount: BigDecimal,
    var reserveDate: LocalDateTime? = LocalDateTime.now(),
    var expDate: LocalDateTime? = null,
    var status: ReservedStatus? = null
)




