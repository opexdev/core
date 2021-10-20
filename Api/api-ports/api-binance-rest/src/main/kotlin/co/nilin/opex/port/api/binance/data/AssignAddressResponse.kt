package co.nilin.opex.port.api.binance.data

data class AssignAddressResponse(
    val address: String,
    val coin: String,
    val tag: String,
    val url: String,
)