package co.nilin.opex.wallet.app.service

import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class UserRegistrationService(
    val walletOwnerManager: WalletOwnerManager,
    val walletManager: WalletManager,
    val currencyService: CurrencyService,
    @Value("\${app.gift.symbol}")
    val symbol: String,
    @Value("\${app.gift.amount}")
    val amount: BigDecimal
) {
    @Transactional
    suspend fun registerNewUser(event: UserCreatedEvent): Wallet {
        val owner =
            walletOwnerManager.createWalletOwner(event.uuid, "${event.firstName} ${event.lastName}", "1")

        val btcSymbol = currencyService.getCurrency("btc") ?: throw OpexException(OpexError.CurrencyNotFound)
        //TODO REMOVE LATER
        walletManager.createWallet(
            owner,
            Amount(btcSymbol, BigDecimal.ONE),
            btcSymbol,
            "main"
        )

        val giftSymbol = currencyService.getCurrency(symbol) ?: throw OpexException(OpexError.CurrencyNotFound)
        return walletManager.createWallet(
            owner,
            Amount(giftSymbol, amount),
            giftSymbol,
            "main"
        )
    }
}