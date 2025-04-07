package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class VoucherData(
    val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    val expireDate: LocalDateTime,
    val createDate: LocalDateTime = LocalDateTime.now(),
    var voucherGroup: VoucherGroupData?,
    val usageCount: Long,
)
