package co.nilin.opex.wallet.core.inout

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletType

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

data class TransferResultDetailed(
        val transferResult: TransferResult,
        val tx: String
)