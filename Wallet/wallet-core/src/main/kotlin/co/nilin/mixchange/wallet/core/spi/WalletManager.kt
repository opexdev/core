package co.nilin.mixchange.wallet.core.spi

import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.model.Currency
import co.nilin.mixchange.wallet.core.model.Wallet
import co.nilin.mixchange.wallet.core.model.WalletOwner
import java.math.BigDecimal

interface WalletManager {
    suspend fun isDepositAllowed(wallet: Wallet, amount: BigDecimal): Boolean
    suspend fun isWithdrawAllowed(wallet: Wallet, amount: BigDecimal): Boolean
    suspend fun increaseBalance(wallet: Wallet, amount: BigDecimal)
    suspend fun decreaseBalance(wallet: Wallet, amount: BigDecimal)
    suspend fun findWalletByOwnerAndCurrencyAndType(owner: WalletOwner, walletType: String, currency: Currency): Wallet?
    suspend fun findWalletsByOwnerAndType(owner: WalletOwner, walletType: String): List<Wallet>
    suspend fun findWalletsByOwner(owner: WalletOwner): List<Wallet>
    suspend fun createWallet(
        owner: WalletOwner,
        balance: Amount,
        currency: Currency,
        type: String
    ): Wallet
}