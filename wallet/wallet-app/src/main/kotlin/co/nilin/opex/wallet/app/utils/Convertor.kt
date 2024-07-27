package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.DepositMethod
import co.nilin.opex.wallet.core.inout.WithdrawMethod
import java.math.BigDecimal
import java.util.*

fun CurrencyCommand.toDto(): CurrencyDto {
    return CurrencyDto(symbol,
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
            depositMethods,
            withdrawMethods,
            externalUrl,
            isCryptoCurrency,
            impls,
            withdrawMin)
}



