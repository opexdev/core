package co.nilin.mixchange.wallet.core.spi

import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.model.WalletOwner

interface WalletOwnerManager {
    suspend fun isDepositAllowed(owner: WalletOwner, amount: Amount): Boolean
    suspend fun isWithdrawAllowed(owner: WalletOwner, amount: Amount): Boolean
    suspend fun findWalletOwner(uuid: String): WalletOwner?
    suspend fun createWalletOwner(uuid: String, title: String,  userLevel: String): WalletOwner
}
