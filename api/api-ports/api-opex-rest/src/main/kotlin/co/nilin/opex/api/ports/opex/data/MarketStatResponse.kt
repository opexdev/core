package co.nilin.opex.api.ports.opex.data

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat

data class MarketStatResponse(
    val mostIncreasedPrice: List<PriceStat>,
    val mostDecreasedPrice: List<PriceStat>,
    val mostVolume: TradeVolumeStat?,
    val mostTrades: TradeVolumeStat?
)