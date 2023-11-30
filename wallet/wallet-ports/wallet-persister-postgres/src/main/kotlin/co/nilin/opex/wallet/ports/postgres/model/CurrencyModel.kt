package co.nilin.opex.wallet.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("currency")
data class CurrencyModel(
        @Id @Column("symbol") var symbol: String,
        @Column("name") var name: String,
        @Column("precision") var precision: BigDecimal,
        var title: String? = null,
        var alias: String? = null,
        @Column("max_deposit") var maxDeposit: BigDecimal? = BigDecimal.TEN,
        @Column("min_deposit") var minDeposit: BigDecimal? = BigDecimal.ZERO,
        @Column("min_withdraw") var minWithdraw: BigDecimal? = BigDecimal.TEN,
        @Column("max_withdraw") var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
        var icon: String? = null,
        @Column("create_date") var createDate: LocalDateTime? = LocalDateTime.now(),
        @Column("last_update_date") var lastUpdateDate: LocalDateTime? = LocalDateTime.now(),
        @Column("is_transitive") var isTransitive:Boolean?=false

)