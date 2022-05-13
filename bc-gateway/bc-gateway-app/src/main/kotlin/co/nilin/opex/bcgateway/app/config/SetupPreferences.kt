package co.nilin.opex.bcgateway.app.config

import co.nilin.opex.bcgateway.ports.postgres.dao.*
import co.nilin.opex.bcgateway.ports.postgres.model.*
import co.nilin.opex.utility.preferences.AddressType
import co.nilin.opex.utility.preferences.Chain
import co.nilin.opex.utility.preferences.Currency
import co.nilin.opex.utility.preferences.ProjectPreferences
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@DependsOn("postgresConfig")
class SetupPreferences(
    private val addressTypeRepository: AddressTypeRepository,
    private val chainRepository: ChainRepository,
    private val chainAddressTypeRepository: ChainAddressTypeRepository,
    private val chainEndpointRepository: ChainEndpointRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyImplementationRepository: CurrencyImplementationRepository,
    private val chainSyncScheduleRepository: ChainSyncScheduleRepository,
    private val walletSyncScheduleRepository: WalletSyncScheduleRepository
) {
    @Autowired
    private lateinit var preferences: ProjectPreferences

    @Autowired
    fun init() {
        runBlocking {
            addAddressTypes(preferences.addressTypes)
            addChains(preferences.chains)
            addCurrencies(preferences.currencies)
            addSchedules(preferences)
        }
    }

    private suspend fun addAddressTypes(data: List<AddressType>) = coroutineScope {
        val items = data.mapIndexed { i, it ->
            if (addressTypeRepository.existsById(i + 1L).awaitSingle())
                null
            else AddressTypeModel(null, it.addressType, it.addressRegex, null)
        }.filterNotNull()
        runCatching { addressTypeRepository.saveAll(items).collectList().awaitSingleOrNull() }
    }

    private suspend fun addChains(data: List<Chain>) = coroutineScope {
        data.map { chainRepository.insert(it.name).awaitSingleOrNull() }
        val items1 = data.map {
            val addressTypeId = addressTypeRepository.findByType(it.addressType).awaitSingle().id!!
            ChainAddressTypeModel(null, it.name, addressTypeId)
        }
        runCatching { chainAddressTypeRepository.saveAll(items1).collectList().awaitSingleOrNull() }
        val items2 = data.map { ChainEndpointModel(null, it.name, it.endpointUrl, null, null) }
        runCatching { chainEndpointRepository.saveAll(items2).collectList().awaitSingleOrNull() }
    }

    private suspend fun addCurrencies(data: List<Currency>) = coroutineScope {
        coroutineScope {
            data.forEach {
                currencyRepository.insert(it.name, it.symbol).awaitSingleOrNull()
            }
        }
        val items =
            data.flatMap { it.implementations.map { impl -> it to impl } }.map { (currency, impl) ->
                CurrencyImplementationModel(
                    null,
                    currency.symbol,
                    impl.chain,
                    impl.token,
                    impl.tokenAddress,
                    impl.tokenName,
                    impl.withdrawEnabled,
                    impl.withdrawFee,
                    impl.withdrawMin,
                    impl.decimal
                )
            }
        runCatching { currencyImplementationRepository.saveAll(items).collectList().awaitSingleOrNull() }
    }

    private suspend fun addSchedules(data: ProjectPreferences) = coroutineScope {
        data.chains.map {
            chainSyncScheduleRepository.insert(it.name, it.schedule.delay.toInt(), it.schedule.errorDelay.toInt())
                .awaitSingleOrNull()
        }
        if (walletSyncScheduleRepository.existsById(1).awaitSingle()) null
        else {
            val item = WalletSyncScheduleModel(
                null,
                LocalDateTime.now(),
                data.systemWallet.schedule.delay,
                data.systemWallet.schedule.batchSize
            )
            runCatching { walletSyncScheduleRepository.save(item).awaitSingleOrNull() }
        }
    }
}
