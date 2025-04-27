package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.otc.ReservedStatus
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




