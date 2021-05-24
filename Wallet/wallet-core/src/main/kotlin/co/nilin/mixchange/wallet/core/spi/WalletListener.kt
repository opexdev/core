package co.nilin.mixchange.wallet.core.spi

import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.model.Wallet
import java.math.BigDecimal

interface WalletListener {
    fun onDeposit(me: Wallet, sourceWallet: Wallet, amount: Amount, finalAmount: BigDecimal, transaction: String)
    fun onWithdraw(me: Wallet, destWallet: Wallet, amount: Amount, transaction: String)
}