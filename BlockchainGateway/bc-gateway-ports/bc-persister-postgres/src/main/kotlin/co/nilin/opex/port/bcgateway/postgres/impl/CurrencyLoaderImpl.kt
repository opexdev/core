package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.Currency
import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyImplementationRepository
import co.nilin.opex.port.bcgateway.postgres.dao.CurrencyRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull

class CurrencyLoaderImpl(
    private val currencyRepository: CurrencyRepository,
    private val currencyImplementationRepository: CurrencyImplementationRepository
) : CurrencyLoader {
    override suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo {
        val currencyDao = currencyRepository.findBySymbol(symbol).awaitSingleOrNull()
        val currencyImplDao = currencyImplementationRepository.findBySymbol(symbol)
        val currency = Currency(currencyDao.symbol, currencyDao.name)
        return CurrencyInfo(currency, currencyImplDao.map {
            CurrencyImplementation(
                currency,
                Chain(it.chain, emptyList(), emptyList()),
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
        return currencyImplementationRepository.findByChainAndAddress(chain, address)
            .awaitFirstOrNull()?.symbol
    }

    override suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation> {
        return currencyImplementationRepository.findByChain(chain).map {
            val currencyDao = currencyRepository.findBySymbol(it.symbol).awaitSingleOrNull()
            val currency = Currency(currencyDao.symbol, currencyDao.name)
            CurrencyImplementation(
                currency,
                Chain(it.chain, emptyList(), emptyList()),
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
