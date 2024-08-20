package co.nilin.opex.bcgateway.app.config

import co.nilin.opex.bcgateway.ports.postgres.dao.*
import co.nilin.opex.bcgateway.ports.postgres.model.AddressTypeModel
import co.nilin.opex.bcgateway.ports.postgres.model.ChainAddressTypeModel
import co.nilin.opex.bcgateway.ports.postgres.model.CurrencyImplementationModel
import co.nilin.opex.utility.preferences.AddressType
import co.nilin.opex.utility.preferences.Chain
import co.nilin.opex.utility.preferences.Currency
import co.nilin.opex.utility.preferences.Preferences
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(
        private val addressTypeRepository: AddressTypeRepository,
        private val chainRepository: ChainRepository,
        private val chainAddressTypeRepository: ChainAddressTypeRepository,
//    private val currencyRepository: CurrencyRepository,
        private val currencyImplementationRepository: CurrencyImplementationRepository,
) {
    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        addAddressTypes(preferences.addressTypes)
        addChains(preferences.chains)
        addCurrencies(preferences.currencies)
    }

    private suspend fun addAddressTypes(data: List<AddressType>) = coroutineScope {
        val items = data.mapIndexed { i, it ->
            if (addressTypeRepository.existsById(i + 1L).awaitSingle()) null
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
    }

    private suspend fun addCurrencies(data: List<Currency>) = coroutineScope {
//        coroutineScope {
//            data.forEach {
//                currencyRepository.insert(it.name, it.symbol).awaitSingleOrNull()
//            }
//        }
        val items = data.flatMap { it.implementations.map { impl -> it to impl } }.map { (currency, impl) ->
            CurrencyImplementationModel(
                    null,
                    UUID.randomUUID().toString(),
                    currency.symbol,
                    impl.symbol.takeUnless { it.isEmpty() } ?: currency.symbol,
                    impl.chain,
                    impl.token,
                    impl.tokenAddress,
                    impl.tokenName,
                    impl.withdrawEnabled,
                    true!!,
                    impl.withdrawFee,
                    impl.withdrawMin,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    impl.decimal,
                    true

            )
        }
        runCatching { currencyImplementationRepository.saveAll(items).collectList().awaitSingleOrNull() }
    }
}
