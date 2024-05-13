package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
//import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

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
                    doSave(it.toDto().updateTo(request).toModel().apply { id = it.id })?.toDto()
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



    override suspend fun fetchCurrencies(request: FetchCurrency): CurrenciesCommand? {
        TODO("Not yet implemented")
    }



}