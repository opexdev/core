package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("currency_new")
data class NewCurrencyModel(
        @Id
        var id:Long,
        var symbol: String,
        var uuid: String? = UUID.randomUUID().toString(),
        var name: String,
        var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        var icon: String? = null,
        @Column("is_transitive")
        var isTransitive: Boolean? = false,
        @Column("is_active")
        var isActive: Boolean? = true,
        var sign: String? = null,
        var description: String? = null,
        @Column("short_description")
        var shortDescription: String? = null,
        @Column("is_withdraw_enable")
        var isWithdrawEnable: Boolean? = true,
        @Column("withdraw_fee")
        var withdrawFee: BigDecimal?,
        @Column("external_url")
        var externalUrl: String? = null,
        @Column("is_crypto_currency")
        var isCryptoCurrency: Boolean? = false
)