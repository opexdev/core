package co.nilin.opex.admin.core.events

data class AddCurrencyEvent(
    val name: String,
    val symbol: String,
    val precision: Double
) : AdminEvent()