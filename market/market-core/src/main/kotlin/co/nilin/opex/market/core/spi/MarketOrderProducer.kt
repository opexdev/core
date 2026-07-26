package co.nilin.opex.market.core.spi

interface MarketOrderProducer {

    suspend fun openOrderUpdate(uuid: String, pair: String)
}