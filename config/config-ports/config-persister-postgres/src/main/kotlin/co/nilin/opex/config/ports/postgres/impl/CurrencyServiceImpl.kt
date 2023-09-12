package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CurrencyServiceImpl(val currencyRepository: CurrencyRepository) : CurrencyService {

    private val logger = LoggerFactory.getLogger(CurrencyServiceImpl::class.java)

    override suspend fun getCurrency(symbol: String): Currency? {
        return currencyRepository.findBySymbol(symbol).awaitFirstOrNull()?.run {
            Currency(this.symbol, name, precision)
        }
    }

    override suspend fun addCurrency(name: String, symbol: String, precision: BigDecimal) {
        try {
            currencyRepository.insert(name, symbol, precision).awaitSingleOrNull()
        } catch (e: Exception) {
            logger.error("Could not insert new currency $name", e)
        }
    }

    override suspend fun editCurrency(name: String, symbol: String, precision: BigDecimal) {
        val currency = currencyRepository.findById(name).awaitFirstOrNull()
        if (currency != null) {
            currency.symbol = symbol
            currency.precision = precision
            currencyRepository.save(currency).awaitFirst()
        }
    }

    override suspend fun deleteCurrency(name: String) {
        try {
            currencyRepository.deleteByName(name).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Could not delete currency $name", e)
        }
    }
}