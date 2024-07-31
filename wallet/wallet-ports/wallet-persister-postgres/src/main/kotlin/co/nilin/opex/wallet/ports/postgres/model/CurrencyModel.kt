package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("currency")
data class CurrencyModel(
    @Id var symbol: String,
    var name: String,
    var precision: BigDecimal,
    var title: String? = null,
    var alias: String? = null,
    var maxDeposit: BigDecimal? = BigDecimal.TEN,
    var minDeposit: BigDecimal? = BigDecimal.ZERO,
    var minWithdraw: BigDecimal? = BigDecimal.TEN,
    var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
    var icon: String? = null,
    var createDate: LocalDateTime? = LocalDateTime.now(),
    var lastUpdateDate: LocalDateTime? = LocalDateTime.now(),
    var isTransitive: Boolean? = false,
    var isActive: Boolean? = true,
    var sign: String? = null,
    var description: String? = null,
    var shortDescription: String? = null
)