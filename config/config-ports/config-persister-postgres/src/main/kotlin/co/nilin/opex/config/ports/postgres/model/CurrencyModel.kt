package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency")
data class CurrencyModel(
    @Id @Column("symbol") var symbol: String,
    @Column("name") var name: String,
    @Column("precision") var precision: BigDecimal
)