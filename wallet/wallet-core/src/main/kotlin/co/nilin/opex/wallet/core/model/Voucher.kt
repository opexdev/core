package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Voucher(
    val id: Long? = null,
    val privateCode: String,
    val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    val expireDate: LocalDateTime,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val voucherGroup: VoucherGroup
)
