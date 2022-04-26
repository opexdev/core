package co.nilin.opex.admin.core.events

data class EditCurrencyEvent(
    val name: String,
    val symbol: String,
    val precision: Double
) : AdminEvent()