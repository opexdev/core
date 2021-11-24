package co.nilin.opex.api.ports.binance.data

import java.math.BigDecimal

data class DepositResponse(
    val amount: BigDecimal,
    val coin: String,
    val network: String,
    val status: Int,
    val address: String,
    val addressTag: String?,
    val txId: String,
    val insertTime: Long,
    val transferType: Int,
    val unlockConfirm: String,
    val confirmTimes: String,
    val time: Long
)