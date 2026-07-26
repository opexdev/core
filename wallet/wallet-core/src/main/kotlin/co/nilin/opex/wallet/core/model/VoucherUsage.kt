package co.nilin.opex.wallet.core.model

import java.time.LocalDateTime

data class VoucherUsage(
    val voucher: Long,
    var useDate: LocalDateTime,
    var uuid: String,
)
