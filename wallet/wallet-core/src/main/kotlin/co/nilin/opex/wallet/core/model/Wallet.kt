package co.nilin.opex.wallet.core.model

import co.nilin.opex.wallet.core.inout.CurrencyCommand

data class Wallet(
    val id: Long?,
    val owner: WalletOwner,
    val balance: Amount,
    val currency: Currency,
    val type: String,
    val version: Long?
)