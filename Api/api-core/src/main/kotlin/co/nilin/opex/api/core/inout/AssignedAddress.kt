package co.nilin.opex.api.core.inout

data class Endpoint(val url: String)
data class AddressType(val id: Long, val type: String, val addressRegex: String, val memoRegex: String)
data class Chain(val name: String, val addressTypes: List<AddressType>, val endpoints: List<Endpoint>)
data class AssignedAddress(
    val uuid: String,
    val address: String,
    val memo: String?,
    val type: AddressType,
    val chains: MutableList<Chain>
)
