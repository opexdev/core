package co.nilin.opex.price.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("pair_provider_include")
data class PairProviderIncludeModel(
    @Id val id: Int? = null,
    @Column("symbol") val symbol: String,
    @Column("provider") val provider: String
)
