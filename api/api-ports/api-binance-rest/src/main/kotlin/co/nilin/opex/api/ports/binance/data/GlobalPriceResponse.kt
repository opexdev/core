package co.nilin.opex.api.ports.binance.data

import co.nilin.opex.api.core.inout.CurrencyRate
import co.nilin.opex.api.core.inout.GlobalPrice

data class GlobalPriceResponse(
    val usdRate: List<CurrencyRate>,
    val prices: List<GlobalPrice>
)