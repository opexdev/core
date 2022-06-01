package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("assigned_address_chains")
data class AssignedAddressChainModel(
    @Id val id: Long?,
    @Column("assigned_address_id") val addressTypeId: Long,
    @Column("chain") val chain: String
)
