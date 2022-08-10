package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.PriceStat
import co.nilin.opex.api.core.inout.TradeVolumeStat

interface MarketStatProxy {

    suspend fun getMostIncreasedInPricePairs(interval: Long, limit: Int): List<PriceStat>

    suspend fun getMostDecreasedInPricePairs(interval: Long, limit: Int): List<PriceStat>

    suspend fun getHighestVolumePair(interval: Long): TradeVolumeStat?

    suspend fun getTradeCountPair(interval: Long): TradeVolumeStat?

}