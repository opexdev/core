package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class SellVoucherRequest(
    val publicCode: String,
    val nationalCode: String,
    val phoneNumber: String,
    val transactionNumber: String,
    val transactionAmount: BigDecimal,
)
