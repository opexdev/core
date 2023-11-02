package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet

data class TransferCommand(
    val sourceWallet: Wallet,
    val destWallet: Wallet,
    val amount: Amount,
    val description: String?,
    val transferRef: String?,
    val transferCategory: String,
    val additionalData: Map<String, Any>?,
    val destAmount: Amount = amount
)
