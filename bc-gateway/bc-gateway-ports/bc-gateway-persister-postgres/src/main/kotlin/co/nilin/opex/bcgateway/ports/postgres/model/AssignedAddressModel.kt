package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("assigned_addresses")
data class AssignedAddressModel(
    @Id val id: Long?,
    val uuid: String,
    val address: String,
    val memo: String?,
    @Column("addr_type_id") val addressTypeId: Long
)

