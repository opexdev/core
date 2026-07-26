package co.nilin.opex.market.ports.kafka.producer.events

import co.nilin.opex.market.core.inout.MarketOrderEvent

data class OpenOrderUpdateEvent(val uuid: String, val pair: String) : MarketOrderEvent()