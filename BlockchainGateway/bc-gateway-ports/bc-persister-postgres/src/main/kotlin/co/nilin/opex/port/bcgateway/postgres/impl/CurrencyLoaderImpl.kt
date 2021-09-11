package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import co.nilin.opex.port.bcgateway.postgres.dao.ChainRepository
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyRepository
import co.nilin.opex.port.bcgateway.postgres.model.CurrencyImplementationModel
import co.nilin.opex.port.bcgateway.postgres.model.CurrencyModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class CurrencyLoaderImpl(
    private val chainRepository: ChainRepository,
    private val currencyRepository: CurrencyRepository,
    private val currencyImplementationRepository: CurrencyImplementationRepository
) : CurrencyLoader {
    override suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo? {
        val currencyDao = currencyRepository.findBySymbol(symbol).awaitSingleOrNull()
        return if (currencyDao !== null) {
            val currencyImplDao = currencyImplementationRepository.findBySymbol(symbol)
            val currency = Currency(currencyDao.symbol, currencyDao.name)
            val implementations = currencyImplDao.map { projectCurrencyImplementation(it, currencyDao) }
            CurrencyInfo(currency, implementations.toList())
        } else {
            null
        }
    }

    override suspend fun findSymbol(chain: String, address: String?): String? {
        return currencyImplementationRepository.findByChainAndTokenAddress(chain, address)
            .awaitFirstOrNull()?.symbol
    }

    override suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation> {
        return currencyImplementationRepository.findByChain(chain).map { projectCurrencyImplementation(it) }.toList()
    }

    private suspend fun projectCurrencyImplementation(
        implDao: CurrencyImplementationModel,
        currencyDao: CurrencyModel? = null
    ): CurrencyImplementation {
        val addressTypesDao = chainRepository.findAddressTypesByName(implDao.chain)
        val addressTypes = addressTypesDao.map { AddressType(it.id!!, it.type, it.addressRegex, it.memoRegex) }
        val endpointsDao = chainRepository.findEndpointsByName(implDao.chain)
        val endpoints = endpointsDao.map { Endpoint(it.url) }
        val currencyDaoVal = currencyDao ?: currencyRepository.findBySymbol(implDao.symbol).awaitSingle()
        val currency = Currency(currencyDaoVal.symbol, currencyDaoVal.name)
        return CurrencyImplementation(
            currency,
            Chain(implDao.chain, addressTypes.toList(), endpoints.toList()),
            implDao.token,
            implDao.tokenAddress,
            implDao.tokenName,
            implDao.withdrawEnabled,
            implDao.withdrawFee,
            implDao.withdrawMin
        )
    }
}
