package co.nilin.opex.port.bcgateway.walletproxy.model

data class Currency(
    val symbol: String,
    val name: String,
    val precision: Int,
)