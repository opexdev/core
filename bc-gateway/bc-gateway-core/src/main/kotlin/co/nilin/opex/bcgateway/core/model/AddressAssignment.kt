package co.nilin.opex.bcgateway.core.model

import java.time.LocalDateTime

data class AddressAssignmentFilter(
    val uuid: String? = null,
    val address: String? = null,
    val memo: String? = null,
    val status: AddressStatus? = null,
    val addressTypeId: Long? = null,
    val chain: String? = null,
    val assignedFrom: Long? = null,
    val assignedTo: Long? = null,
    val limit: Int = 20,
    val offset: Int = 0
)

data class AddressAssignmentRecord(
    val id: Long,
    val uuid: String,
    val address: String,
    val memo: String?,
    val addressType: AddressType?,
    val assignedDate: LocalDateTime?,
    val expTime: LocalDateTime?,
    val revokedDate: LocalDateTime?,
    val status: AddressStatus?
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
    val addressType: AddressType?
)

data class ReservedAddressPage(
    val total: Long,
    val items: List<ReservedAddressRecord>
)
