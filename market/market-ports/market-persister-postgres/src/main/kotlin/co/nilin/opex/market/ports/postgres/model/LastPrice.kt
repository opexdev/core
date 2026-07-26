package co.nilin.opex.market.ports.postgres.model

import java.math.BigDecimal

data class LastPrice(val symbol: String, val matchedPrice: BigDecimal)