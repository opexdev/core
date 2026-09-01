package co.nilin.opex.price.core.service

import java.math.BigDecimal
import java.math.MathContext

/**
 * Flags provider prices that deviate too far from the group's median — the standard,
 * distribution-agnostic way price oracles (e.g. Chainlink-style aggregators) reject a bad quote
 * among a handful of sources. Median is used instead of mean because a single wild outlier can't
 * drag it away the way it would an average, so it stays a fair "what does everyone else say"
 * reference point even while one provider is broken.
 *
 * With fewer than 3 prices there's no way to tell *which* one is wrong (with 2 disagreeing prices,
 * either could be the bad one), so filtering is skipped and everything is kept as-is.
 */
object PriceOutlierFilter {

    fun <T> partition(
        items: List<Pair<T, BigDecimal>>,
        thresholdPercent: BigDecimal
    ): Pair<List<Pair<T, BigDecimal>>, List<Pair<T, BigDecimal>>> {
        if (items.size < 3) return items to emptyList()

        val median = median(items.map { it.second })
        if (median == BigDecimal.ZERO) return items to emptyList()

        return items.partition { (_, price) ->
            val deviationPercent = (price - median).abs()
                .divide(median, MathContext.DECIMAL64)
                .multiply(BigDecimal(100))
            deviationPercent <= thresholdPercent
        }
    }

    private fun median(values: List<BigDecimal>): BigDecimal {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]).divide(BigDecimal(2), MathContext.DECIMAL64)
        } else {
            sorted[mid]
        }
    }
}
