package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyOnChainGatewayLocalizationCommand
import co.nilin.opex.bcgateway.core.model.CurrencyOnChainGatewayView
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayLocalizationModel
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyOnChainGatewayModel


fun CryptoCurrencyCommand.toModel(): CurrencyOnChainGatewayModel {
    return CurrencyOnChainGatewayModel(
        null, gatewayUuid!!,
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
        isDepositActive,
        isWithdrawActive,
        displayOrder,
    )
}

fun CurrencyOnChainGatewayModel.toDto(): CryptoCurrencyCommand {

    return CryptoCurrencyCommand(
        currencySymbol,
        gatewayUuid!!,
        implementationSymbol,
        isDepositActive,
        isWithdrawActive,
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
        chain,
        null,//todo
        null,//todo
        displayOrder,
    )

}

fun CurrencyOnChainGatewayView.toDto(): CryptoCurrencyCommand {

    return CryptoCurrencyCommand(
        currencySymbol,
        gatewayUuid,
        implementationSymbol,
        isDepositActive,
        isWithdrawActive,
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
        chain,
        depositDescription,
        withdrawDescription,
        displayOrder,
    )

}

fun CurrencyOnChainGatewayLocalizationModel.toCommand(): CurrencyOnChainGatewayLocalizationCommand {
    return CurrencyOnChainGatewayLocalizationCommand(
        id = id,
        depositDescription = depositDescription,
        withdrawDescription = withdrawDescription,
        language = language
    )
}