package co.nilin.opex.wallet.ports.postgres.model


import co.nilin.opex.wallet.core.model.Currency
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("currency")
data class CurrencyModel(
    @JsonIgnore @Id @Column("symbol") var symbol_: String,
    @JsonIgnore @Column("name") val name_: String,
    @JsonIgnore @Column("precision") var precision_: Double
) : Currency {

    override fun getSymbol(): String {
        return symbol_
    }

    override fun getName(): String {
        return name_
    }

    override fun getPrecision(): Double {
        return precision_
    }
}

