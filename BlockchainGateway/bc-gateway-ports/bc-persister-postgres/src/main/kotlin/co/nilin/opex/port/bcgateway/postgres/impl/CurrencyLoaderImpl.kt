package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import co.nilin.opex.port.bcgateway.postgres.dao.ChainRepository
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class CurrencyLoaderImpl(
    private val chainRepository: ChainRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyImplementationRepository: CurrencyImplementationRepository
) : CurrencyLoader {
    override suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo {
        val currencyDao = currencyRepository.findBySymbol(symbol).awaitSingleOrNull()
        val currencyImplDao = currencyImplementationRepository.findBySymbol(symbol)
        val currency = Currency(currencyDao.symbol, currencyDao.name)
        return CurrencyInfo(currency, currencyImplDao.map {
            val addressTypesDao = chainRepository.findAddressTypesByName(it.chain)
            val addressTypes = addressTypesDao.map { addressType ->
                AddressType(addressType.id!!, addressType.type, addressType.addressRegex, addressType.memoRegex)
            }
            val endpointsDao = chainRepository.findEndpointsByName(it.chain)
            val endpoints = endpointsDao.map { endpoint ->
                Endpoint(endpoint.url)
            }
            CurrencyImplementation(
                currency,
                Chain(it.chain, addressTypes.toList(), endpoints.toList()),
                it.token,
                it.tokenAddress,
                it.tokenName,
                it.withdrawEnabled,
                it.withdrawFee,
                it.withdrawMin
            )
        }.toList())
    }

    override suspend fun findSymbol(chain: String, address: String?): String? {
        return currencyImplementationRepository.findByChainAndTokenAddress(chain, address)
            .awaitFirstOrNull()?.symbol
    }

    override suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation> {
        return currencyImplementationRepository.findByChain(chain).map {
            val currencyDao = currencyRepository.findBySymbol(it.symbol).awaitSingleOrNull()
            val currency = Currency(currencyDao.symbol, currencyDao.name)
            val addressTypesDao = chainRepository.findAddressTypesByName(it.chain)
            val addressTypes = addressTypesDao.map { addressType ->
                AddressType(addressType.id!!, addressType.type, addressType.addressRegex, addressType.memoRegex)
            }
            val endpointsDao = chainRepository.findEndpointsByName(it.chain)
            val endpoints = endpointsDao.map { endpoint ->
                Endpoint(endpoint.url)
            }
            CurrencyImplementation(
                currency,
                Chain(it.chain, addressTypes.toList(), endpoints.toList()),
                it.token,
                it.tokenAddress,
                it.tokenName,
                it.withdrawEnabled,
                it.withdrawFee,
                it.withdrawMin
            )
        }.toList()
    }
}
