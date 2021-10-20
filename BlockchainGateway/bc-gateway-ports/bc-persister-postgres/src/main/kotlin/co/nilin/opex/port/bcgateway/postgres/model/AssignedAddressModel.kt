package co.nilin.opex.port.bcgateway.postgres.model

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

@Table("assigned_address_chains")
data class AssignedAddressChainModel(
    @Id val id: Long?,
    @Column("assigned_address_id") val addressTypeId: Long,
    @Column("chain") val chain: String
)
