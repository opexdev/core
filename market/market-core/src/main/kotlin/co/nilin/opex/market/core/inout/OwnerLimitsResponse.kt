package co.nilin.opex.market.core.inout

data class OwnerLimitsResponse(
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean
)