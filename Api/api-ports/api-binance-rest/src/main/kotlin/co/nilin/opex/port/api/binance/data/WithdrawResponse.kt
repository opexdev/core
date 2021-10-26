package co.nilin.opex.port.api.binance.data

import java.math.BigDecimal

data class WithdrawResponse(
    val address: String,
    val amount: BigDecimal,
    val applyTime: String,
    val coin: String,
    val id: String,
    val withdrawOrderId: String,
    val network: String,
    val transferType: Int,
    val status: Int,
    val transactionFee: String,
    val confirmNo: Int,
    val txId: String,
    val time: Long
)