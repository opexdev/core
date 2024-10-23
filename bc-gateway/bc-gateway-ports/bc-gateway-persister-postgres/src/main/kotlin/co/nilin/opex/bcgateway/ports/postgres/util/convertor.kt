package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel


fun CryptoCurrencyCommand.toModel(): CurrencyOnChainGatewayModel {
    return CurrencyOnChainGatewayModel(null, gatewayUuid!!,
            currencySymbol,
            implementationSymbol,
            chain,
            isToken,
            tokenAddress,
            tokenName,
            withdrawAllowed!!,
            depositAllowed!!,
            withdrawFee!!,
            withdrawMin,
            withdrawMax,
            depositMin,
            depositMax,
            decimal,
            isActive
    )
}

fun CurrencyOnChainGatewayModel.toDto(): CryptoCurrencyCommand {

    return CryptoCurrencyCommand(currencySymbol,
            gatewayUuid!!,
            implementationSymbol,
            isActive,
            isToken,
            tokenName,
            tokenAddress,
            withdrawFee,
            withdrawAllowed,
            depositAllowed,
            withdrawMin,
            withdrawMax,
            depositMin,
            depositMax,
            decimal,
            chain)

}

