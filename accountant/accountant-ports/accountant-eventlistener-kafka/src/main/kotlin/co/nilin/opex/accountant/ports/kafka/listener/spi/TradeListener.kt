package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent

interface TradeListener : Listener<TradeEvent>