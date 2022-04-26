package co.nilin.opex.wallet.core.inout

data class PagingWithdrawResponse(
    val total: Long,
    val withdraws: List<WithdrawResponse>
)