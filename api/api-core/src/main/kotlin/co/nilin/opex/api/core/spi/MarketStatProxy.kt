package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat
import co.nilin.opex.common.utils.Interval

interface MarketStatProxy {

    fun getMostIncreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat>

    fun getMostDecreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat>

    fun getHighestVolumePair(interval: Interval): TradeVolumeStat?

    fun getTradeCountPair(interval: Interval): TradeVolumeStat?

}