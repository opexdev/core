package co.nilin.opex.accountant.ports.walletproxy.data

import co.nilin.opex.accountant.core.model.WalletType

data class TransferResult(
    val date: Long,
    val sourceUuid: String,
    val sourceWalletType: WalletType,
    val sourceBalanceBeforeAction: Amount,
    val sourceBalanceAfterAction: Amount,
    val amount: Amount,
    val destUuid: String,
    val destWalletType: WalletType,
    val receivedAmount: Amount
)