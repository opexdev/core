package co.nilin.opex.port.wallet.postgres.model


import co.nilin.opex.wallet.core.model.Currency
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency")
class CurrencyModel(
    @JsonIgnore @Id @Column("name") val name_: String,
    @JsonIgnore @Column("symbol") val symbol_: String,
    @JsonIgnore @Column("precision") val precision_: Int
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

