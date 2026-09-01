package co.nilin.opex.price.core.dto

/**
 * AUTO: the scheduler fetches prices from providers, aggregates them (strategy + margin) and
 * writes the result to rate_history — this is the existing behavior.
 * MANUAL: the scheduler skips this symbol entirely (no provider calls at all); the price only
 * changes when an admin submits one explicitly, and stays put until they submit another or
 * switch the symbol back to AUTO.
 */
enum class PriceMode {
    AUTO, MANUAL
}
