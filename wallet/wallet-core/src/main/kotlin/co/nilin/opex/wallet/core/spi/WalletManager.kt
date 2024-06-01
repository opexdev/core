package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.*
import java.math.BigDecimal

interface WalletManager {

    suspend fun isDepositAllowed(wallet: Wallet, amount: BigDecimal): Boolean

    suspend fun isWithdrawAllowed(wallet: Wallet, amount: BigDecimal): Boolean

    suspend fun increaseBalance(wallet: Wallet, amount: BigDecimal)

    suspend fun decreaseBalance(wallet: Wallet, amount: BigDecimal)

    suspend fun findWalletByOwnerAndCurrencyAndType(owner: WalletOwner, walletType: String, currency: CurrencyCommand): Wallet?

    suspend fun findWalletsByOwnerAndType(owner: WalletOwner, walletType: String): List<Wallet>

    suspend fun findWalletsByOwner(owner: WalletOwner): List<Wallet>

    suspend fun findWalletByOwnerAndSymbol(owner: WalletOwner, symbol: String): List<Wallet>

    suspend fun createWallet(
        owner: WalletOwner,
        balance: Amount,
        currency: CurrencyCommand,
        type: String
    ): Wallet

    suspend fun findWalletById(walletId: Long): Wallet?

    suspend fun findAllWalletsBriefNotZero(ownerId: Long): List<BriefWallet>
}