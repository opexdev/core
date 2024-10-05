package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.model.*
import java.math.BigDecimal

interface WalletManager {

    suspend fun isDepositAllowed(wallet: Wallet, amount: BigDecimal): Boolean

    suspend fun isWithdrawAllowed(wallet: Wallet, amount: BigDecimal): Boolean

    suspend fun increaseBalance(wallet: Wallet, amount: BigDecimal)

    suspend fun decreaseBalance(wallet: Wallet, amount: BigDecimal)

    suspend fun findWalletByOwnerAndCurrencyAndType(
        owner: WalletOwner,
        walletType: WalletType,
        currency: CurrencyCommand
    ): Wallet?

    suspend fun findWallet(ownerId: Long, currency: String, walletType: WalletType): BriefWallet?

    suspend fun findWalletsByOwnerAndType(owner: WalletOwner, walletType: WalletType): List<Wallet>

    suspend fun findWalletsByOwner(owner: WalletOwner): List<Wallet>

    suspend fun findWalletByOwnerAndSymbol(owner: WalletOwner, symbol: String): List<Wallet>

    suspend fun createWallet(owner: WalletOwner, balance: Amount, currency: CurrencyCommand, type: WalletType): Wallet


    suspend fun createWalletForSystem(
            currency: String
    )

    suspend fun findWalletById(walletId: Long): Wallet?

    suspend fun findAllWalletsBriefNotZero(ownerId: Long): List<BriefWallet>
}