package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("address_types")
data class AddressTypeModel(
    val id: Long?,
    @Column("address_type") val type: String,
    @Column("address_regex") val addressRegex: String,
    @Column("memo_regex") val memoRegex: String
)