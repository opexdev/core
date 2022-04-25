package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("reserved_addresses")
data class ReservedAddressModel(
    val id: Long?,
    val address: String,
    val memo: String?,
    @Column("address_type") val type: Long
)