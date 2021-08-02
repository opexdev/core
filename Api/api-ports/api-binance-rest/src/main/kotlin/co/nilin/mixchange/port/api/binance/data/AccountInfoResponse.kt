package co.nilin.mixchange.port.api.binance.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AccountInfoResponse(
    val makerCommission: Long,
    val takerCommission: Long,
    val buyerCommission: Long,
    val sellerCommission: Long,
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean,
    val updateTime: Long,
    val accountType: String, // Enum
    val balances: List<BalanceResponse>,
    val permissions: List<String> // Enum
)