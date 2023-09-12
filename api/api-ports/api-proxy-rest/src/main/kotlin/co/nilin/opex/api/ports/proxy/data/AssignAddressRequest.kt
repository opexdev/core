package co.nilin.opex.api.ports.proxy.data

data class AssignAddressRequest(
    val uuid: String,
    val currency: String,
    val chain: String,
)