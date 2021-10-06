package co.nilin.opex.wallet.app.listener

import co.nilin.opex.port.wallet.postgres.dao.WithdrawRepository
import co.nilin.opex.port.wallet.postgres.model.WithdrawModel
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
        if (me.type().equals("cashout")) {
            val fee = (additionalData?.get("fee") ?: "0").toBigDecimal()
            withdrawRepository.save(
                WithdrawModel(
                    null, transaction , me.id()!!, finalAmount, fee, finalAmount.subtract(fee), additionalData?.get("destCurrency"), additionalData?.get("destAddress"), additionalData?.get("destNote"), null, additionalData?.get("description"), null, "CREATED"
                )
            ).awaitFirst()
        }
    }

    override suspend fun onWithdraw(me: Wallet, destWallet: Wallet, amount: Amount, transaction: String, additionalData: Map<String, String?>?) {
        if (me.type().equals("cashout")) {
            val withdrawModel = withdrawRepository.findByWalletAndTransactionId(
                me.id()!!, additionalData!!.get("transactionId") ?: throw RuntimeException("transactionId is required")
            ).awaitFirstOrElse { throw RuntimeException("No matching withdraw request") }
            if (withdrawModel!!.status != "CREATED") {
                throw RuntimeException("This withdraw request processed before")
            }
            val newStatus = additionalData.get("status") ?: throw RuntimeException("status is required")
            withdrawModel.status = newStatus
            withdrawModel.statusReason = additionalData.get("statusReason")
            if ( additionalData.get("destNote") != null) {
                withdrawModel.destNote += "\n---------------\n" + additionalData.get("destNote")
            }
            if (newStatus == "DONE") {
                withdrawModel.destTransactionRef = additionalData.get("destTransactionRef")
            }
            withdrawRepository.save(withdrawModel).awaitFirst()
        }
    }
}