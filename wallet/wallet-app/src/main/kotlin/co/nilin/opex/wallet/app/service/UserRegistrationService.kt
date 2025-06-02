package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class UserRegistrationService(
    val walletOwnerManager: WalletOwnerManager,
    val walletManager: WalletManager,
    val currencyService: CurrencyServiceManager
) {

    private val logger = LoggerFactory.getLogger(UserRegistrationService::class.java)

    @Transactional
    suspend fun registerNewUser(event: UserCreatedEvent) {
        val title = "${event.username} | ${event.email ?: ""} - ${event.mobile ?: ""}"
        val owner = walletOwnerManager.createWalletOwner(event.uuid, title, "1") //TODO define proper user levels

        val currencies = currencyService.fetchCurrencies()?.currencies ?: run {
            logger.warn("Could not fetch currencies")
            return
        }

        currencies.forEach { walletManager.createWallet(owner, Amount(it, BigDecimal.ZERO), it, WalletType.MAIN) }
    }
}
