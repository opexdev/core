package co.nilin.opex.wallet.core.model

data class Wallet(
    val id: Long?,
    val owner: WalletOwner,
    val balance: Amount,
    val currency: Currency,
    val type: String
)