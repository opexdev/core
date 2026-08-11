package co.nilin.opex.matching.gateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("pair_category")
data class PairCategoryModel(
    @Id val id: Long? = null,
    val pair: String,
    val category: PairCategory
)