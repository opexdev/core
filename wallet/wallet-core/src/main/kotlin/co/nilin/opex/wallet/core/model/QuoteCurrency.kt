package co.nilin.opex.wallet.core.model

import java.time.LocalDateTime

data class QuoteCurrency(
    val currency: String,
    val isActive: Boolean = false,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
)