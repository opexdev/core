package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel



fun CurrencyCommand.toModel(): CurrencyModel {
    return CurrencyModel(
            symbol,
            uuid,
            name,
            precision,
            title,
            alias,
            icon,
            isTransitive,
            isActive,
            sign,
            description,
            shortDescription,
            withdrawIsEnable,
            depositIsEnable,
            withdrawFee,
            externalUrl,
            isCryptoCurrency

    )
}

fun CurrencyModel.toCommand(): CurrencyCommand {
    return CurrencyCommand(symbol,
            uuid,
            name,
            precision,
            title,
            alias,
            icon,
            isTransitive,
            isActive,
            sign,
            description,
            shortDescription,
            withdrawAllowed,
            depositAllowed,
            withdrawFee,
            null,
            null,
            externalUrl,
            isCryptoCurrency,
            null,
            )
}