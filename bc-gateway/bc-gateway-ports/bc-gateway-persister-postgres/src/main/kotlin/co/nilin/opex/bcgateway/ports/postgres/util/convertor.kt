package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import org.modelmapper.ModelMapper
import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal

@Column("impl_uuid") val implUuid: String,
@Column("currency_symbol") val currencySymbol: String,
@Column("implementation_symbol") var implementationSymbol: String,
@Column("chain") var chain: String,
@Column("is_token") var isToken: Boolean,
@Column("token_address") var tokenAddress: String?,
@Column("token_name") var tokenName: String?,
@Column("withdraw_allowed") var withdrawAllowed: Boolean,
@Column("withdraw_fee") var withdrawFee: BigDecimal,
@Column("withdraw_min") var withdrawMin: BigDecimal,
@Column("decimal") var decimal: Int,
@Column("is_active") var isActive: Boolean?=true,
fun CryptoCurrencyCommand.toModel(): CurrencyImplementationModel {
    return CurrencyImplementationModel(
            null,
            impUuid,
            currencySymbol,
            implementationCurrencySymbol,
            chain.name,
            isToken,
            tokenAddress,
            tokenName,
            withdrawAllowed,
            withdrawFee,
            withdrawMin,
            decimal,
            isActive



    )
}


fun CurrencyImplementationModel.toDto():CryptoCurrencyCommand{
    return ModelMapper().map(this,CryptoCurrencyCommand::class.java)
}