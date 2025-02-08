package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.VoucherGroup
import co.nilin.opex.wallet.core.model.VoucherStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class VoucherData(
    val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    var status: VoucherStatus,
    val expireDate: LocalDateTime,
    val createDate: LocalDateTime = LocalDateTime.now(),
    var useDate: LocalDateTime? = null,
    var uuid: String? = null,
    var voucherGroup: VoucherGroup?,
)
