package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.model.WalletOwner
import co.nilin.opex.wallet.ports.postgres.model.WalletModel

fun WalletModel.toPlainObject(walletOwner: WalletOwner, currency: CurrencyCommand) = Wallet(
    id,
    walletOwner,
    Amount(currency, balance),
    currency,
    type,
    version
)

fun Wallet.toModel() = WalletModel(
    id,
    owner.id!!,
    type,
    currency.symbol!!,
    balance.amount
)