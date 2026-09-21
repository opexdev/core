package co.nilin.opex.price.core.dto

import java.math.BigDecimal
import java.math.MathContext

enum class PriceStrategy {
    MIN, MAX, AVERAGE;

    fun apply(prices: List<BigDecimal>): BigDecimal = when (this) {
        MIN -> prices.min()
        MAX -> prices.max()
        AVERAGE -> prices.reduce(BigDecimal::add).divide(BigDecimal(prices.size), MathContext.DECIMAL64)
    }
}
