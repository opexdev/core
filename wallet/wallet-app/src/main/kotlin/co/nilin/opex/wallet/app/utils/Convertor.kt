package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.core.inout.CurrencyCommand

fun CurrencyCommand.toDto(): CurrencyDto {
    return CurrencyDto(
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
        withdrawAllowed ?: false,
        depositAllowed ?: false,
        externalUrl,
        gateways,
        availableGatewayType,
        order
    )
}



