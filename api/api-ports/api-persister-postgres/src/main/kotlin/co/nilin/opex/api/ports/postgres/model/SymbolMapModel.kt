package co.nilin.opex.api.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("symbol_maps")
class SymbolMapModel(
    @Id val symbol: String,
    @Column("value") val value: String,
)