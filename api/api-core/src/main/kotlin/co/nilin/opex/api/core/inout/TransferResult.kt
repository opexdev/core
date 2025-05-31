package co.nilin.opex.api.core.inout

data class TransferResult(
    val date: Long,
    val sourceUuid: String,
    val sourceWalletType: WalletType,
    val sourceBalanceBeforeAction: Amount,
    val sourceBalanceAfterAction: Amount,
    val amount: Amount,
    val destUuid: String,
    val destWalletType: WalletType,
    val receivedAmount: Amount,
    val sourceWallet: Long? = null,
    val destWallet: Long? = null
)