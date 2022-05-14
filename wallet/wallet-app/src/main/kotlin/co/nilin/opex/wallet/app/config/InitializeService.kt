package co.nilin.opex.wallet.app.config

import co.nilin.opex.utility.preferences.Currency
import co.nilin.opex.utility.preferences.Preferences
import co.nilin.opex.utility.preferences.UserLimit
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dao.UserLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.UserLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(
    @Value("\${app.system.uuid}") val systemUuid: String,
    private val currencyRepository: CurrencyRepository,
    private val walletOwnerRepository: WalletOwnerRepository,
    private val walletRepository: WalletRepository,
    private val userLimitsRepository: UserLimitsRepository
) {
    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        addCurrencies(preferences.currencies)
        addSystemWallet(preferences)
        addUserLimits(preferences.userLimits)
    }

    private suspend fun addUserLimits(data: List<UserLimit>) = coroutineScope {
        data.forEachIndexed { i, it ->
            if (!userLimitsRepository.existsById(i + 1L).awaitSingle()) {
                runCatching {
                    userLimitsRepository.save(
                        UserLimitsModel(
                            null,
                            it.level,
                            it.owner,
                            it.action,
                            it.walletType,
                            it.dailyTotal,
                            it.dailyCount,
                            it.monthlyTotal,
                            it.monthlyCount
                        )
                    ).awaitSingleOrNull()
                }
            }
        }
    }

    private suspend fun addSystemWallet(p: Preferences) = coroutineScope {
        if (!walletOwnerRepository.existsById(1).awaitSingle()) {
            walletOwnerRepository.save(WalletOwnerModel(null, systemUuid, p.system.walletTitle, p.system.walletLevel))
                .awaitSingleOrNull()
        }
        val items = p.currencies.flatMap {
            listOf(
                WalletModel(null, 1, "main", it.symbol, it.mainBalance),
                WalletModel(null, 1, "exchange", it.symbol, BigDecimal.ZERO)
            )
        }
        runCatching { walletRepository.saveAll(items).collectList().awaitSingleOrNull() }
    }

    private suspend fun addCurrencies(data: List<Currency>) = coroutineScope {
        data.forEach {
            currencyRepository.insert(it.name, it.symbol, it.precision.toDouble()).awaitSingleOrNull()
        }
    }
}
