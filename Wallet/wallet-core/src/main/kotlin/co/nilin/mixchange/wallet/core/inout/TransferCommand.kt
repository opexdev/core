package co.nilin.mixchange.wallet.core.inout

import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.model.Wallet

data class TransferCommand(val sourceWallet: Wallet, val destWallet: Wallet, val amount: Amount, val description: String?, val transferRef: String?)
