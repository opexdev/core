package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel

fun CurrencyModel.toPlainObject() = Currency(
    symbol,
    name,
    precision
)

fun Currency.toModel() = CurrencyModel(
    symbol,
    name,
    precision
)