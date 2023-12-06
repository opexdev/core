package co.nilin.opex.bcgateway.ports.walletproxy.model

data class Currency(
    val symbol: String,
    val name: String,
    val precision: Int,
)