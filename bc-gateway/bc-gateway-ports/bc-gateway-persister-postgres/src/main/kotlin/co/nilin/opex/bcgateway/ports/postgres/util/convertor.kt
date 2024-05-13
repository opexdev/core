package co.nilin.opex.bcgateway.ports.postgres.util

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.ports.postgres.model.NewCurrencyImplementationModel
import org.modelmapper.ModelMapper

fun CryptoCurrencyCommand.toModel():NewCurrencyImplementationModel{
    return ModelMapper().map(this,NewCurrencyImplementationModel::class.java)
}


fun NewCurrencyImplementationModel.toDto():CryptoCurrencyCommand{
    return ModelMapper().map(this,CryptoCurrencyCommand::class.java)
}