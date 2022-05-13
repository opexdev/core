package co.nilin.opex.wallet.app.service

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.preferences.ProjectPreferences
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.math.BigDecimal

@Component
class UserRegistrationService(
    val walletOwnerManager: WalletOwnerManager,
    val walletManager: WalletManager,
    val currencyService: CurrencyService,
    @Value("\${app.preferences}") val file: File
) {
    @Transactional
    suspend fun registerNewUser(event: UserCreatedEvent) {
        val mapper = ObjectMapper(YAMLFactory())
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)

        val owner = walletOwnerManager.createWalletOwner(event.uuid, "${event.firstName} ${event.lastName}", "1")

        p.currencies.filter { it.gift > BigDecimal.ZERO }.forEach {
            val currency = currencyService.getCurrency(it.symbol) ?: throw OpexException(OpexError.CurrencyNotFound)
            walletManager.createWallet(owner, Amount(currency, it.gift), currency, "main")
        }
    }
}
