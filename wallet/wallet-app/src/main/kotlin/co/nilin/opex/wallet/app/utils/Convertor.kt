package co.nilin.opex.wallet.app.utils

import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import org.modelmapper.ModelMapper

fun CurrencyCommand.toDto():CurrencyDto{
    return ModelMapper().map(this,CurrencyDto::class.java)
}


