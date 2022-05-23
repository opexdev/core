package co.nilin.opex.wallet.ports.kafka.listener.model

import java.math.BigDecimal

data class EditCurrencyEvent(
    val name: String,
    val symbol: String,
    val precision: BigDecimal
) : AdminEvent()