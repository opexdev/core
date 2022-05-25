package co.nilin.opex.admin.core.events

import java.math.BigDecimal

data class EditCurrencyEvent(
    val name: String,
    val symbol: String,
    val precision: BigDecimal
) : AdminEvent()