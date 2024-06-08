package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

class WithdrawCommand(
    val uuid: String,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val transferRef: String?,
    val destSymbol: String,
    val destAddress: String,
    val destNetwork: String?,
    val destNote: String?,
    val acceptedFee: BigDecimal
)