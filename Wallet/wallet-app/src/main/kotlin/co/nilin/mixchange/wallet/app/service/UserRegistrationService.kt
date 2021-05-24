package co.nilin.mixchange.wallet.app.service

import co.nilin.mixchange.auth.gateway.model.UserCreatedEvent
import co.nilin.mixchange.wallet.app.controller.Symbol
import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.model.Wallet
import co.nilin.mixchange.wallet.core.spi.WalletManager
import co.nilin.mixchange.wallet.core.spi.WalletOwnerManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class UserRegistrationService(
    val walletOwnerManager: WalletOwnerManager,
    val walletManager: WalletManager,
    @Value("\${app.gift.symbol}")
    val symbol: Symbol,
    @Value("\${app.gift.amount}")
    val amount: BigDecimal
) {
    @Transactional
    suspend fun registerNewUser(event: UserCreatedEvent): Wallet {
        val owner =
            walletOwnerManager.createWalletOwner(event.uuid, "${event.firstName} ${event.lastName}", "1")
        return walletManager.createWallet(
            owner,
            Amount(symbol, amount),
            symbol,
            "main"
        )
    }
}