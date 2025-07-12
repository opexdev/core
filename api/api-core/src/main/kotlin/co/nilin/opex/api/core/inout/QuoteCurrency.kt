package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class QuoteCurrency(
    val currency: String,
    val isActive: Boolean = false,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
)