package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal
import java.util.*

data class VoucherSaleDataResponse(
    val publicCode: String,
    val nationalCode: String,
    val phoneNumber: String,
    val transactionNumber: String,
    val transactionAmount: BigDecimal,
    val saleDate: Date?,
    val sellerUuid: String,
)
