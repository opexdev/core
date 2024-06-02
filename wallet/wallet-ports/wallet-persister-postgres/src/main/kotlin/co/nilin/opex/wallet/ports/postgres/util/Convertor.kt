package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel



fun CurrencyCommand.toModel(): NewCurrencyModel {
    return NewCurrencyModel(
            null,
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

fun NewCurrencyModel.toCommand(): CurrencyCommand {
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
            withdrawIsEnable,
            depositIsEnable,
            withdrawFee,
            null,
            null,
            externalUrl,
            isCryptoCurrency,
            null,
            id)
}