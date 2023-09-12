package co.nilin.opex.wallet.core.model

data class WalletOwner(
    val id: Long?,
    val uuid: String,
    val title: String,
    val level: String,
    val isTradeAllowed: Boolean,
    val isWithdrawAllowed: Boolean,
    val isDepositAllowed: Boolean
)