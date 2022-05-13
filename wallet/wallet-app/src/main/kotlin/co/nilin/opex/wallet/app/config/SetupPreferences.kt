package co.nilin.opex.wallet.app.config

import co.nilin.opex.utility.preferences.ProjectPreferences
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dao.UserLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletRepository
import co.nilin.opex.wallet.ports.postgres.model.UserLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.io.File
import java.math.BigDecimal

@Component
@DependsOn("postgresConfig")
class SetupPreferences(
    @Value("preferences.yml") file: File,
    private val currencyRepository: CurrencyRepository,
    private val walletOwnerRepository: WalletOwnerRepository,
    private val walletRepository: WalletRepository,
    private val userLimitsRepository: UserLimitsRepository
) {
    private val mapper = ObjectMapper(YAMLFactory())

    init {
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)
        runBlocking {
            addCurrencies(p)
            addSystemWallet(p)
            addUserLimits(p)
        }
    }

    private suspend fun addUserLimits(p: ProjectPreferences) = coroutineScope {
        p.userLimits.forEachIndexed { i, it ->
            launch {
                if (!userLimitsRepository.existsById(i + 1L).awaitSingle()) {
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

    private suspend fun addSystemWallet(p: ProjectPreferences) = coroutineScope {
        if (!walletOwnerRepository.existsById(1).awaitSingle()) {
            walletOwnerRepository.save(
                WalletOwnerModel(
                    null,
                    p.systemWallet.uuid,
                    p.systemWallet.title,
                    p.systemWallet.level
                )
            ).awaitSingleOrNull()
        }
        launch {
            val items1 = p.currencies.map {
                WalletModel(
                    null,
                    1,
                    "main",
                    it.symbol,
                    it.mainBalance
                )
            }
            val items2 = p.currencies.map {
                WalletModel(
                    null,
                    1,
                    "exchange",
                    it.symbol,
                    BigDecimal.ZERO
                )
            }
            walletRepository.saveAll(items1.zip(items2).flatMap { it.toList() }).collectList().awaitSingleOrNull()
        }
    }

    private suspend fun addCurrencies(p: ProjectPreferences) = coroutineScope {
        p.currencies.forEach {
            launch {
                currencyRepository.insert(it.name, it.symbol, it.precision.toDouble()).awaitSingleOrNull()
            }
        }
    }
}
