package co.nilin.opex.admin.core.events

import java.math.BigDecimal

data class AddCurrencyEvent(
    val name: String,
    val symbol: String,
    val precision: BigDecimal
) : AdminEvent()