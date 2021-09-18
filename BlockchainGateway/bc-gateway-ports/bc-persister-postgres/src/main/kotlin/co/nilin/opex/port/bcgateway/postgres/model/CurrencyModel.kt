package co.nilin.opex.port.bcgateway.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency")
class CurrencyModel(
    @Id @Column("symbol") val symbol: String,
    @Column("name") val name: String
)
