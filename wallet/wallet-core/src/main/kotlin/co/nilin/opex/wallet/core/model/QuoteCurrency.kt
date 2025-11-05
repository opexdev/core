package co.nilin.opex.wallet.core.model

import java.time.LocalDateTime

data class QuoteCurrency(
    val currency: String,
    val isReference: Boolean = false,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
    val displayOrder: Int? = null
)