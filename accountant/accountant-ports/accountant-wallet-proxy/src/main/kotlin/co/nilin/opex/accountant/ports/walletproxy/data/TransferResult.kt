package co.nilin.opex.accountant.ports.walletproxy.data

data class TransferResult(
    val date: Long,
    val sourceUuid: String,
    val sourceWalletType: String,
    val sourceBalanceBeforeAction: Amount,
    val sourceBalanceAfterAction: Amount,
    val amount: Amount,
    val destUuid: String,
    val destWalletType: String,
    val receivedAmount: Amount
)