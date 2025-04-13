package co.nilin.opex.wallet.app.dto

import java.time.LocalDateTime

data class VoucherUsageDataResponse(
    var useDate: LocalDateTime,
    var uuid: String,
)
