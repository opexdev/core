package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.preferences.Preferences
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserRegistrationService(
    val walletOwnerManager: WalletOwnerManager,
    val walletManager: WalletManager,
    val currencyService: CurrencyServiceManager
) {
    @Autowired
    private lateinit var preferences: Preferences

    @Transactional
    suspend fun registerNewUser(event: UserCreatedEvent) {
        val owner =
            walletOwnerManager.createWalletOwner(event.uuid, "${event.email}-${event.firstName} ${event.lastName}", "1")

        preferences.currencies.forEach {
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = it.symbol))
                ?: throw OpexError.CurrencyNotFound.exception()
            walletManager.createWallet(owner, Amount(currency, it.gift), currency, WalletType.MAIN)
        }
    }
}
