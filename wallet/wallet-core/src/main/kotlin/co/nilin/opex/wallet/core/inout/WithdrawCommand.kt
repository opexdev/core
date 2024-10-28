package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.WithdrawType
import java.math.BigDecimal

class WithdrawCommand(
    val uuid: String,
    var currency: String,
    val amount: BigDecimal,
    val description: String?,
    var destSymbol: String?,
    val destAddress: String,
    var destNetwork: String?,
    val destNote: String?,
    val gatewayUuid: String?,
    var withdrawType: WithdrawType?
)