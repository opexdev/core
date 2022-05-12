package co.nilin.opex.bcgateway.app.config

import co.nilin.opex.bcgateway.ports.postgres.dao.*
import co.nilin.opex.bcgateway.ports.postgres.model.*
import co.nilin.opex.utility.preferences.ProjectPreferences
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime

@Component
class SetupPreferences(
    @Value("preferences.yml") file: File,
    addressTypeRepository: AddressTypeRepository,
    chainRepository: ChainRepository,
    chainAddressTypeRepository: ChainAddressTypeRepository,
    chainEndpointRepository: ChainEndpointRepository,
    currencyRepository: CurrencyRepository,
    currencyImplementationRepository: CurrencyImplementationRepository,
    chainSyncScheduleRepository: ChainSyncScheduleRepository,
    walletSyncScheduleRepository: WalletSyncScheduleRepository
) {
    init {
        val mapper = ObjectMapper(YAMLFactory())
        val p: ProjectPreferences = mapper.readValue(file, ProjectPreferences::class.java)
        runBlocking {
            // Add address types
            addressTypeRepository.saveAll(p.addressTypes.mapIndexed { i, it ->
                AddressTypeModel(i + 1L, it.addressType, it.addressRegex, null)
            }).awaitFirst()

            // Add chains with endpoints
            chainRepository.saveAll(p.chains.mapIndexed { i, it ->
                ChainModel(it.name)
            }).awaitFirst()
            chainAddressTypeRepository.saveAll(p.chains.mapIndexed { i, it ->
                val addressTypeId = addressTypeRepository.findByType(it.addressType).awaitSingle().id!!
                ChainAddressTypeModel(i + 1L, it.name, addressTypeId)
            }).awaitFirst()
            chainEndpointRepository.saveAll(p.chains.mapIndexed { i, it ->
                ChainEndpointModel(i + 1L, it.name, it.endpointUrl, null, null)
            }).awaitFirst()

            // Add currencies with implementations
            currencyRepository.saveAll(p.currencies.mapIndexed { i, it ->
                CurrencyModel(it.symbol, it.name)
            }).awaitFirst()
            currencyImplementationRepository.saveAll(p.currencies.flatMap { listOf(it).zip(it.implementations) }
                .mapIndexed { i, (currency, impl) ->
                    CurrencyImplementationModel(
                        i + 1L,
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
                }).awaitFirst()

            // Add sync schedules. Wallet and Chain
            chainSyncScheduleRepository.saveAll(p.chains.mapIndexed { i, it ->
                ChainSyncScheduleModel(it.name, LocalDateTime.now(), it.schedule.delay, it.schedule.errorDelay)
            })
            walletSyncScheduleRepository.save(p.systemWallet.let {
                WalletSyncScheduleModel(1, LocalDateTime.now(), it.schedule.delay, it.schedule.batchSize)
            })
        }
    }
}
