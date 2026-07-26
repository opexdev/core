package co.nilin.opex.api.ports.opex.data

import java.time.LocalDateTime

data class AssignAddressResponse(
    val address: String,
    val currency: String,
    var expTime: LocalDateTime? = null,
    var assignedDate: LocalDateTime? = null,
)