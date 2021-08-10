package co.nilin.opex.port.wallet.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency_rate")
class CurrencyRateModel(
    @Id val id: Long?,
    @Column("source_currency") val sourceCurrency: String,
    @Column("dest_currency") val destCurrency: String,
    @Column("rate") val rate: Double
)