package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat
import co.nilin.opex.common.utils.Interval

interface MarketStatProxy {

    suspend fun getMostIncreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat>

    suspend fun getMostDecreasedInPricePairs(interval: Interval, limit: Int): List<PriceStat>

    suspend fun getHighestVolumePair(interval: Interval): TradeVolumeStat?

    suspend fun getTradeCountPair(interval: Interval): TradeVolumeStat?

}