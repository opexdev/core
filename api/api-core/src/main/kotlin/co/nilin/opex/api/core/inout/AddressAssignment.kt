package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class AddressAssignmentFilter(
    val uuid: String? = null,
    val address: String? = null,
    val memo: String? = null,
    val status: AddressAssignmentStatus? = null,
    val addressTypeId: Long? = null,
    val chain: String? = null,
    val assignedFrom: Long? = null,
    val assignedTo: Long? = null,
    val limit: Int = 20,
    val offset: Int = 0
)

enum class AddressAssignmentStatus {
    Assigned, Revoked, Reserved
}

data class AddressTypeInfo(
    val id: Long,
    val type: String
)

data class AddressAssignmentRecord(
    val id: Long,
    val uuid: String,
    val address: String,
    val memo: String?,
    val addressType: AddressTypeInfo?,
    val assignedDate: LocalDateTime?,
    val expTime: LocalDateTime?,
    val revokedDate: LocalDateTime?,
    val status: AddressAssignmentStatus?,
    val fullName: String? = null
)

data class AddressAssignmentPage(
    val total: Long,
    val items: List<AddressAssignmentRecord>
)

data class AdminAssignAddressRequest(
    val uuid: String,
    val address: String? = null,
    val memo: String? = null,
    val expTime: Long? = null
)

data class ReservedAddressFilter(
    val addressTypeId: Long? = null,
    val chain: String? = null,
    val address: String? = null,
    val limit: Int = 20,
    val offset: Int = 0
)

data class ReservedAddressRecord(
    val address: String,
    val memo: String?,
    val addressType: AddressTypeInfo?
)

data class ReservedAddressPage(
    val total: Long,
    val items: List<ReservedAddressRecord>
)
