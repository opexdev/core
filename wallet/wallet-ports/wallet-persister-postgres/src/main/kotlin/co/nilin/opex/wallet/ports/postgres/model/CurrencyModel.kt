package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("currency")
data class CurrencyModel(
    @Id
    var symbol: String,
    var uuid: String? = UUID.randomUUID().toString(),
    var precision: BigDecimal,
    var icon: String? = null,
    @Column("is_transitive")
    var isTransitive: Boolean? = false,
    @Column("is_active")
    var isActive: Boolean? = true,
    var sign: String? = null,
    @Column("external_url")
    var externalUrl: String? = null,
    @Column("display_order")
    var displayOrder: Int? = null,
    var maxOrder : BigDecimal? = null,
)