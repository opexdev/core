package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.Amount

data class TransferResult(
        val date: Long,
        val sourceUuid: String,
        val sourceWalletType: String,
        val sourceBalanceBeforeAction: Amount,
        val sourceBalanceAfterAction: Amount,
        val amount: Amount,
        val destUuid: String,
        val destWalletType: String,
        val receivedAmount: Amount,
        val sourceWalletId: Long? = null,
        val destinationWalletId: Long? = null)

data class TransferResultDetailed(
        val transferResult: TransferResult,
        val tx: String
)