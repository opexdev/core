package co.nilin.opex.wallet.app.listener

import co.nilin.opex.wallet.ports.postgres.dao.WithdrawRepository
import co.nilin.opex.wallet.ports.postgres.model.WithdrawModel
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.spi.WalletListener
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.math.BigDecimal

@Component
class WalletListenerImpl(val withdrawRepository: WithdrawRepository) : WalletListener {
    override suspend fun onDeposit(
        me: Wallet,
        sourceWallet: Wallet,
        amount: Amount,
        finalAmount: BigDecimal,
        transaction: String,
        additionalData: Map<String, String?>?
    ) {

    }

    override suspend fun onWithdraw(me: Wallet, destWallet: Wallet, amount: Amount, transaction: String, additionalData: Map<String, String?>?) {

    }
}