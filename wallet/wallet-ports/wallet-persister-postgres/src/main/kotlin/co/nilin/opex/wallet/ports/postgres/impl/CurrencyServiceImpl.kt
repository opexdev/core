package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.spi.CurrencyService
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.stream.Collector
import java.util.stream.Collectors

@Service
class CurrencyServiceImpl(val currencyRepository: CurrencyRepository) : CurrencyService {

    private val logger = LoggerFactory.getLogger(CurrencyServiceImpl::class.java)

    override suspend fun getCurrency(symbol: String): Currency? {

        return currencyRepository.findBySymbol(symbol).awaitFirstOrNull()?.let { it.toDto() }
                ?: throw OpexException(OpexError.CurrencyNotFound)

    }


    override suspend fun addCurrency(name: String, symbol: String, precision: BigDecimal) {
        try {
            addCurrency(Currency(symbol, name, precision))
        } catch (e: Exception) {
            logger.error("Could not insert new currency $name", e)
        }
    }


    override suspend fun addCurrency(request: Currency): Currency? {

        currencyRepository.findBySymbol(request.symbol)?.awaitSingleOrNull()?.let {
            throw OpexException(OpexError.CurrencyIsExist)
        } ?: run {
            try {
                val cm = request.toModel()
                return currencyRepository.insert(cm.name, cm.symbol, cm.precision,
                        cm.title, cm.alias, cm.maxDeposit, cm.minDeposit, cm.minWithdraw, cm.maxWithdraw,
                        cm.icon, cm.createDate, cm.lastUpdateDate).awaitSingleOrNull()?.toDto()
            } catch (e: Exception) {
                logger.error("Could not insert new currency ${request.symbol}", e)
                throw OpexException(OpexError.Error)
            }
        }

    }

    override suspend fun updateCurrency(request: Currency): Currency? {

        currencyRepository.findBySymbol(request.symbol)?.awaitSingleOrNull()?.let {
            try {
                val cm = request.toModel()
                return currencyRepository.save(cm.apply { this.createDate = it.createDate }).awaitSingleOrNull()?.toDto()
            } catch (e: Exception) {
                logger.error("Could not update currency ${request.symbol}", e)
                throw OpexException(OpexError.Error)
            }
        } ?: throw OpexException(OpexError.CurrencyNotFound)


    }


    private fun Currency.toModel(): CurrencyModel {
        return with(this) {
            CurrencyModel(
                    symbol, name, precision, title, alias, maxDeposit, minDeposit, minWithdraw, maxWithdraw, icon, LocalDateTime.now(), LocalDateTime.now()
            )
        }
    }

    private fun CurrencyModel.toDto(): Currency {
        return with(this) {
            Currency(
                    symbol, name, precision, title, alias, maxDeposit, minDeposit, minWithdraw, maxWithdraw, icon
            )
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

    override suspend fun deleteCurrency(name: String): Currencies {

        return currencyRepository.findBySymbol(name).awaitFirstOrNull()?.let {
            currencyRepository.deleteBySymbol(name).awaitFirstOrNull().let { getCurrencies() }
        } ?: throw OpexException(OpexError.CurrencyNotFound)

    }

    override suspend fun getCurrencies(): Currencies {
        return Currencies(currencyRepository.findAll()?.map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }
}