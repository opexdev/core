package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import org.modelmapper.ModelMapper

fun CurrencyCommand.toModel():NewCurrencyModel{
    return ModelMapper().map(this,NewCurrencyModel::class.java)
}


fun NewCurrencyModel.toDto():CurrencyCommand{
    return ModelMapper().map(this,CurrencyCommand::class.java)
}