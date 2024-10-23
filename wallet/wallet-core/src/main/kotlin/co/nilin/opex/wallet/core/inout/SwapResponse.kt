package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class SwapResponse(
    var reserveNumber: String,
    var sourceSymbol: String,
    var destSymbol: String,
    var uuid: String,
    var sourceAmount: BigDecimal,
    var reservedDestAmount: BigDecimal,
    var reserveDate: LocalDateTime? = LocalDateTime.now(),
    var expDate: LocalDateTime? = null,
    var status: ReservedStatus? = null,
    val rate: BigDecimal? = null,
)
