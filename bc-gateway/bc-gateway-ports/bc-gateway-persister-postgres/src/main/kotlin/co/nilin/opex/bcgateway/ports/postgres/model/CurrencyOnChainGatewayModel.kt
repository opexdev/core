package co.nilin.opex.bcgateway.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_on_chain_gateway")
class CurrencyOnChainGatewayModel(
    @Id var id: Long?,
    @Column("gateway_uuid") val gatewayUuid: String,
    @Column("currency_symbol") val currencySymbol: String,
    @Column("implementation_symbol") var implementationSymbol: String? = currencySymbol,
    @Column("chain") var chain: String,
    @Column("is_token") var isToken: Boolean? = false,
    @Column("token_address") var tokenAddress: String? = null,
    @Column("token_name") var tokenName: String? = null,
    @Column("withdraw_allowed") var withdrawAllowed: Boolean,
    @Column("deposit_allowed") var depositAllowed: Boolean,
    @Column("withdraw_fee") var withdrawFee: BigDecimal,
    @Column("withdraw_min") var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    @Column("withdraw_max") var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    @Column("deposit_min") var depositMin: BigDecimal? = BigDecimal.ZERO,
    @Column("deposit_max") var depositMax: BigDecimal? = BigDecimal.ZERO, @Column("decimal") var decimal: Int,
    @Column("is_deposit_active") var isDepositActive: Boolean? = true,
    @Column("is_withdraw_active") var isWithdrawActive: Boolean? = true,
    @Column("description") val description: String?,
    @Column("display_order") val displayOrder: Int? = null,

    )




