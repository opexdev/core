package co.nilin.opex.accountant.core.spi

import java.math.BigDecimal

interface PairStaticRateLoader {
    suspend fun calculateStaticRate(leftSide: String, rightSide: String): BigDecimal?
}