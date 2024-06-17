package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import org.modelmapper.ModelMapper
import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal


fun CryptoCurrencyCommand.toModel(): CurrencyImplementationModel {
    return CurrencyImplementationModel(
            null,
            impUuid!!,
            currencySymbol,
            implementationCurrencySymbol,
            chain,
            isToken!!,
            tokenAddress,
            tokenName,
            withdrawAllowed!!,
            depositAllowed!!,
            withdrawFee!!,
            withdrawMin!!,
            decimal,
            isActive



    )
}
fun CurrencyImplementationModel.toDto():CryptoCurrencyCommand{
    return ModelMapper().map(this,CryptoCurrencyCommand::class.java)
}