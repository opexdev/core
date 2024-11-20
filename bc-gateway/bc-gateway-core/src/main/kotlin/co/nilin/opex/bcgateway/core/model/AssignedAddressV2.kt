package co.nilin.opex.bcgateway.core.model

import java.time.LocalDateTime

data class AssignedAddressV2(
    val typeId: Long,
    val address: String,
    val memo: String?,
    var expTime: LocalDateTime? = null,
    var assignedDate: LocalDateTime? = null,
    var revokedDate: LocalDateTime? = null,
    var status: AddressStatus? = AddressStatus.Reserved,
    var id: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssignedAddressV2

        if (address != other.address) return false
        if (memo != other.memo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + (memo?.hashCode() ?: 0)
        return result
    }
}
