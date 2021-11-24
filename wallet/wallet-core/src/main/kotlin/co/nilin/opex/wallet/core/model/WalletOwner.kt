package co.nilin.opex.wallet.core.model

interface WalletOwner {
    fun id(): Long?
    fun uuid(): String
    fun title(): String
    fun level(): String
    fun isTradeAllowed(): Boolean
    fun isWithdrawAllowed(): Boolean
    fun isDepositAllowed(): Boolean
}