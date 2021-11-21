package co.nilin.opex.api.ports.binance.data

data class AssignAddressResponse(
    val address: String,
    val coin: String,
    val tag: String,
    val url: String,
)