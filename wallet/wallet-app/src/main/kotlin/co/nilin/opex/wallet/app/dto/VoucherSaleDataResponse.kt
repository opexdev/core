package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class VoucherSaleDataResponse(
    val publicCode: String,
    val nationalCode: String,
    val phoneNumber: String,
    val transactionNumber: String,
    val transactionAmount: BigDecimal,
    val saleDate: LocalDateTime?,
    val sellerUuid: String, 
)
