package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
//import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID
import java.util.stream.Collectors

@Service("newVersion")
class CurrencyServiceImplV2(val currencyRepository: CurrencyRepositoryV2) : CurrencyServiceManager {

    private val logger = LoggerFactory.getLogger(CurrencyServiceImplV2::class.java)

    override suspend fun createNewCurrency(request: CurrencyCommand): CurrencyCommand? {
        return loadCurrencies(FetchCurrency(symbol = request.symbol))?.awaitFirstOrNull()?.let {
            throw OpexError.CurrencyIsExist.exception()
        } ?: run {
            return doSave(request.toModel())?.toDto()
        }
    }


    override suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand? {
        return loadCurrencies(FetchCurrency(uuid = request.uuid))
                ?.awaitFirstOrNull()?.let {
                    doSave(it.toDto().toUpdate(request).toModel().apply { id = it.id })?.toDto()
                } ?: throw OpexError.CurrencyNotFound.exception()

    }

    override suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand? {
        return loadCurrencies(FetchCurrency(uuid = request))?.awaitFirstOrNull()?.let {
            if (it.isCryptoCurrency == false)
                return doSave(it.apply { isCryptoCurrency = true })?.toDto()
            it.toDto()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

    private suspend fun loadCurrencies(request: FetchCurrency): Flux<NewCurrencyModel>? {
        return currencyRepository.fetchCurrencies(request.uuid, request.symbol, request.name)
    }


    private suspend fun doSave(request: NewCurrencyModel): NewCurrencyModel? {
        return currencyRepository.save(request).awaitFirstOrNull()

    }


    override suspend fun editCurrency(name: String, symbol: String, precision: BigDecimal) {
        TODO("Not yet implemented")
    }


    override suspend fun currency2Crypto(request: CryptoCurrencyCommand): CurrencyCommand? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchCurrencies(request: FetchCurrency): CurrenciesCommand? {
        TODO("Not yet implemented")
    }


//    override suspend fun updateCurrency(request: Currency): Currency? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getCurrency(symbol: String): Currency? {
//        TODO("Not yet implemented")
//    }

//    override suspend fun addCurrency(name: String, symbol: String, precision: BigDecimal) {
//        TODO("Not yet implemented")
//    }

//    override suspend fun addCurrency(request: Currency): Currency? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun editCurrency(name: String, symbol: String, precision: BigDecimal) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun deleteCurrency(name: String): Currencies {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getCurrencies(): Currencies {
//        TODO("Not yet implemented")
//    }


}