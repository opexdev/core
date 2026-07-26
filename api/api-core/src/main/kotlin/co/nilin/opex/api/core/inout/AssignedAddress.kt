package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class AddressType(val id: Long, val type: String, val addressRegex: String, val memoRegex: String?)
data class Chain(val name: String, val addressTypes: List<AddressType>)
data class AssignedAddress(
    val uuid: String,
    val address: String,
    val memo: String?,
    val type: AddressType,
    val chains: MutableList<Chain>,
    var expTime: LocalDateTime? = null,
    var assignedDate: LocalDateTime? = null,
)
