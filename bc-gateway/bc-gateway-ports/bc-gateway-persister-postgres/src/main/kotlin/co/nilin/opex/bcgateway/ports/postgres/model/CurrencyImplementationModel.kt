package co.nilin.opex.bcgateway.ports.postgres.model


import co.nilin.opex.bcgateway.core.model.Chain
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_implementations")
class CurrencyImplementationModel(
        @Id var id: Long?,
        @Column("impl_uuid") val implUuid: String,
        @Column("currency_symbol") val currencySymbol: String,
        @Column("implementation_symbol") var implementationSymbol: String?=currencySymbol,
        @Column("chain") var chain: String,
        @Column("is_token") var isToken: Boolean?=false,
        @Column("token_address") var tokenAddress: String?=null,
        @Column("token_name") var tokenName: String?=null,
        @Column("withdraw_allowed") var withdrawAllowed: Boolean,
        @Column("deposit_allowed") var depositAllowed: Boolean,
        @Column("withdraw_fee") var withdrawFee: BigDecimal,
        @Column("withdraw_min") var withdrawMin: BigDecimal?= BigDecimal.ZERO,
        @Column("decimal") var decimal: Int,
        @Column("is_active") var isActive: Boolean?=true,




        )




