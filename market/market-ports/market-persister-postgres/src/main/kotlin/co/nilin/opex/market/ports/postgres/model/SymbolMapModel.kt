package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("symbol_maps")
class SymbolMapModel(
    @Id var id: Long?,
    val symbol: String,
    val aliasKey: String,
    val alias: String,
)
