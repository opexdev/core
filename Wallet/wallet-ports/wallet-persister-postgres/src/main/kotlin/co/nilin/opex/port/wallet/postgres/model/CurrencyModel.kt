package co.nilin.opex.port.wallet.postgres.model


import co.nilin.opex.wallet.core.model.Currency
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency")
class CurrencyModel(
    @Id @Column("name") val name_: String,
    @Column("symbol") val symbol_: String,
    @Column("precision") val precision_: Int
): Currency {
    override fun getSymbol(): String {
        return symbol_
    }

    override fun getName(): String {
        return name_
    }

    override fun getPrecision(): Int {
       return precision_
    }
}

