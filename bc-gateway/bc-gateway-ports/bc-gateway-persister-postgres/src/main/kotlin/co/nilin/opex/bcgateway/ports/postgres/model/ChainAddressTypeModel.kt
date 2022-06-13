package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("chain_address_types")
data class ChainAddressTypeModel(
    @Id val id: Long?, @Column("chain_name") val chainName: String, @Column("addr_type_id") val addressTypeId: Long
)
