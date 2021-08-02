package co.nilin.mixchange.api.core.inout

data class OwnerLimitsResponse(
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean
)