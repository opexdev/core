package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.model.VoucherGroupType
import java.math.BigDecimal
import java.time.LocalDateTime

data class VoucherData(
    val publicCode: String,
    val amount: BigDecimal,
    val currency: String,
    val expireDate: LocalDateTime,
    val type: VoucherGroupType,
    val issuer: String,
    val description: String ? = null,
    val groupStatus : VoucherGroupStatus,
    val usagesCount : Int
)

