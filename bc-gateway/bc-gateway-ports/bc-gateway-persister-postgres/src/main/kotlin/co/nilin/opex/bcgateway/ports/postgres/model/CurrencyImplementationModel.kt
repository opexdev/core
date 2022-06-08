package co.nilin.opex.bcgateway.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_implementations")
class CurrencyImplementationModel(
    @Id var id: Long?,
    @Column("symbol") val symbol: String,
    @Column("chain") val chain: String,
    @Column("token") val token: Boolean,
    @Column("token_address") var tokenAddress: String?,
    @Column("token_name") var tokenName: String?,
    @Column("token_symbol") var tokenSymbol: String?,
    @Column("withdraw_enabled") var withdrawEnabled: Boolean,
    @Column("withdraw_fee") var withdrawFee: BigDecimal,
    @Column("withdraw_min") var withdrawMin: BigDecimal,
    @Column("decimal") var decimal: Int
)
