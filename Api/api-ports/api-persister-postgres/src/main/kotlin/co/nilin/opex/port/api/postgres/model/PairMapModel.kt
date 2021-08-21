package co.nilin.opex.port.api.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("pair_maps")
class PairMapModel(
    @Id var pair: String?,
    @Column("map") val map: String,
)