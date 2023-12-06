package co.nilin.opex.bcgateway.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency")
class CurrencyModel(
    @Id @Column("symbol") val symbol: String,
    @Column("name") var name: String
)
