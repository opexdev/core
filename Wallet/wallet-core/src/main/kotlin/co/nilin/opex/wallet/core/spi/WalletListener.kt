package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import java.math.BigDecimal

interface WalletListener {
    suspend fun onDeposit(
        me: Wallet,
        sourceWallet: Wallet,
        amount: Amount,
        finalAmount: BigDecimal,
        transaction: String,
        additionalData: Map<String, String?>?
    )

    suspend fun onWithdraw(
        me: Wallet,
        destWallet: Wallet,
        amount: Amount,
        transaction: String,
        additionalData: Map<String, String?>?
    )
}