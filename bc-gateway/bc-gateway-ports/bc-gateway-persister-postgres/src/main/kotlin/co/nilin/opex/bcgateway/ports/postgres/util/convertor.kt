package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import org.modelmapper.ModelMapper
import java.math.BigDecimal


fun CryptoCurrencyCommand.toModel(): CurrencyImplementationModel {
    return CurrencyImplementationModel(null, implUuid!!, currencySymbol, implementationSymbol, chain, isToken, tokenAddress, tokenName, withdrawAllowed!!, depositAllowed!!, withdrawFee!!, withdrawMin, decimal, isActive


    )
}

fun CurrencyImplementationModel.toDto(): CryptoCurrencyCommand {

    return CryptoCurrencyCommand(currencySymbol, implUuid!!, implementationSymbol, isActive, isToken, tokenName, tokenAddress, withdrawFee, withdrawAllowed, depositAllowed, withdrawMin, decimal, chain)

}

