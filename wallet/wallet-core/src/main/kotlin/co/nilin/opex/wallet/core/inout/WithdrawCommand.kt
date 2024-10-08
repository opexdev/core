package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

class WithdrawCommand(
        val uuid: String,
        val currency: String,
        val amount: BigDecimal,
        val description: String?,
        var destSymbol: String?,
        val destAddress: String,
        var destNetwork: String?,
        val destNote: String?,
        val gatewayUuid: String?
)