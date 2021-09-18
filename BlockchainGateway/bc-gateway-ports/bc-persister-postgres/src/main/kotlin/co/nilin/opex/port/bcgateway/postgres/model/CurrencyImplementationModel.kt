package co.nilin.opex.port.bcgateway.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_implementations")
class CurrencyImplementationModel(
    @Id val id: Long?,
    @Column("symbol") val symbol: String,
    @Column("chain") val chain: String,
    @Column("token") val token: Boolean,
    @Column("token_address") val tokenAddress: String?,
    @Column("token_name") val tokenName: String?,
    @Column("withdraw_enabled") val withdrawEnabled: Boolean,
    @Column("withdraw_fee") val withdrawFee: BigDecimal,
    @Column("withdraw_min") val withdrawMin: BigDecimal,
)
