package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("cached_addresses")
data class CachedAddressModel(
    val id: Long?, val address: String, val memo: String?, @Column("address_type") val type: Long
)