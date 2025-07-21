package co.nilin.opex.market.core.inout

import java.time.LocalDateTime

open class MarketOrderEvent {

    val time: LocalDateTime = LocalDateTime.now()
}