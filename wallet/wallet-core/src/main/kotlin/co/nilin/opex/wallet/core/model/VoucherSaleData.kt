package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class VoucherSaleData(
    val voucher: Long,
    val nationalCode: String,
    val phoneNumber: String,
    val transactionNumber: String,
    val transactionAmount: BigDecimal,
    val saleDate: LocalDateTime? = LocalDateTime.now(),
    val sellerUuid: String,
)
