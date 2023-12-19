package co.nilin.opex.wallet.app.config

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.preferences.Currency
import co.nilin.opex.utility.preferences.Preferences
import co.nilin.opex.utility.preferences.UserLimit
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(
        @Value("\${app.system.uuid}") val systemUuid: String,
        @Value("b58dc8b2-9c0f-11ee-8c90-0242ac120002") val adminUuid: String,

        private val currencyRepository: CurrencyRepository,
        private val walletOwnerRepository: WalletOwnerRepository,
        private val walletRepository: WalletRepository,
        private val walletLimitsRepository: WalletLimitsRepository
) {
    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        addCurrencies(preferences.currencies)
        addSystemAndAdminWallet(preferences)
        addUserLimits(preferences.userLimits)
    }

    private suspend fun addUserLimits(data: List<UserLimit>) = coroutineScope {
        data.forEachIndexed { i, it ->
            if (!walletLimitsRepository.existsById(i + 1L).awaitSingle()) {
                runCatching {
                    walletLimitsRepository.save(
                            WalletLimitsModel(
                                    null,
                                    it.level,
                                    it.owner,
                                    it.action,
                                    null,
                                    it.walletType,
                                    null,
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

    private suspend fun addSystemAndAdminWallet(p: Preferences) = coroutineScope {
        if (!walletOwnerRepository.existsById(1).awaitSingle()) {
            walletOwnerRepository.save(WalletOwnerModel(null, systemUuid, p.system.walletTitle, p.system.walletLevel))
                    .awaitSingleOrNull()
        }

        val adminWallet: WalletOwnerModel? =
                walletOwnerRepository.findByUuid(adminUuid).awaitSingleOrNull()
                        ?: walletOwnerRepository.save(WalletOwnerModel(null, adminUuid, "admin", p.system.walletLevel))
                                .awaitSingleOrNull()


        val items = p.currencies.flatMap { currency ->
            listOf(
                    WalletModel(null, 1, "main", currency.symbol, currency.mainBalance),
                    WalletModel(null, 1, "exchange", currency.symbol, BigDecimal.ZERO),
                    WalletModel(null, adminWallet?.id!!, "main", currency.symbol, currency.mainBalance),
                    WalletModel(null, adminWallet?.id!!, "exchange", currency.symbol, BigDecimal.ZERO)

            )
        }
        runCatching { walletRepository.saveAll(items).collectList().awaitSingleOrNull() }
    }


    private suspend fun addCurrencies(data: List<Currency>) = coroutineScope {
        data.forEach {
            currencyRepository.insert(it.name, it.symbol, it.precision).awaitSingleOrNull()
        }
    }
}
