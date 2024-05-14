package co.nilin.opex.bcgateway.ports.postgres.model


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

@Table("new_currency_implementations")
class NewCurrencyImplementationModel(
        @Id var id: Long?,
        @Column("currency_uuid") val currencyUuid: String,
        //todo unique
        @Column("uuid") val currencyImplUuid: String,
        @Column("implementation_symbol") var implementationSymbol: String,
        @Column("chain") var chain: String,
        @Column("is_token") var isToken: Boolean?=false,
        @Column("is_active") var isActive: Boolean?=true,
        @Column("token_address") var tokenAddress: String?,
        @Column("token_name") var tokenName: String?,
        @Column("withdraw_is_enable") var withdrawIsEnable: Boolean?=true,
        @Column("withdraw_fee") var withdrawFee: BigDecimal,
        @Column("withdraw_min") var withdrawMin: BigDecimal,
        @Column("decimal") var decimal: Int,
        @Column("deposit_is_enable") var depositIsEnable: Boolean? = true

)
