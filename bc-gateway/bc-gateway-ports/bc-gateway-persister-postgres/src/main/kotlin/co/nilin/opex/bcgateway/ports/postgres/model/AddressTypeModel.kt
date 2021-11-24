package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("address_types")
data class AddressTypeModel(
    @Id val id: Long?,
    @Column("address_type") val type: String,
    @Column("address_regex") val addressRegex: String,
    @Column("memo_regex") val memoRegex: String?
)