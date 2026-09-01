package co.nilin.opex.price.core.dto

/**
 * A provider an admin has explicitly selected to be used when aggregating a symbol's price
 * (a whitelist, chosen together with the symbol's pair_rate_config) — replaces the old
 * exclude-list design.
 */
data class PairProviderInclude(
    val symbol: String,
    val provider: String
)
