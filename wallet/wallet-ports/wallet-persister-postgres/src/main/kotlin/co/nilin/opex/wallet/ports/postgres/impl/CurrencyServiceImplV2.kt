package co.nilin.opex.wallet.ports.postgres.impl

//import co.nilin.opex.wallet.core.model.Currencies
//import co.nilin.opex.wallet.core.model.Currency
//import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrencyData
import co.nilin.opex.wallet.core.inout.CurrencyPrecision
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.wallet.ports.postgres.util.toCommand
import co.nilin.opex.wallet.ports.postgres.util.toCurrencyData
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Service("newVersion")
class CurrencyServiceImplV2(val currencyRepository: CurrencyRepositoryV2, val redisCacheHelper: RedisCacheHelper) :
    CurrencyServiceManager {
    private val logger = LoggerFactory.getLogger(CurrencyServiceImplV2::class.java)


    override suspend fun createNewCurrency(request: CurrencyCommand, ignoreIfExist: Boolean?): CurrencyCommand? {
        return loadCurrency(FetchCurrency(symbol = request.symbol))?.awaitFirstOrNull()?.let {
            if (!ignoreIfExist!!)
                throw OpexError.CurrencyIsExist.exception()
            else
                return null
        } ?: run {
            return doPersist(request.toModel())?.toCommand()
                .also { redisCacheHelper.put("${request.symbol}-precision", request.precision) }
        }
    }


    override suspend fun updateCurrency(request: CurrencyCommand): CurrencyCommand? {
        return loadCurrency(FetchCurrency(symbol = request.symbol))
            ?.awaitFirstOrNull()?.let {
                doSave(it.toCommand().updateTo(request).toModel())?.toCommand()
                    .also { redisCacheHelper.put("${request.symbol}-precision", request.precision) }
            } ?: throw OpexError.CurrencyNotFound.exception()

    }

//    override suspend fun prepareCurrencyToBeACryptoCurrency(request: String): CurrencyCommand? {
//        return loadCurrency(FetchCurrency(symbol = request))?.awaitFirstOrNull()?.let {
//            if (it.isCryptoCurrency == false)
//                return doSave(it.apply { isCryptoCurrency = true })?.toCommand()
//            it.toCommand()
//        } ?: throw OpexError.CurrencyNotFound.exception()
//    }

    override suspend fun deleteCurrency(request: FetchCurrency): Void? {
        return loadCurrency(request)?.awaitFirstOrNull()?.let {
            currencyRepository.deleteById(it.symbol!!)?.awaitFirstOrNull()
                .also { redisCacheHelper.evict("${request.symbol}-precision") }
        }
    }

    override suspend fun fetchCurrencies(request: FetchCurrency?): CurrenciesCommand? {
        return CurrenciesCommand(loadCurrencies(request)?.map { it.toCommand() }
            ?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun fetchAllCurrencies(): List<CurrencyData> {
        return currencyRepository.fetchAll()
            .map {
                it.toCurrencyData()
            }
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun fetchCurrency(request: FetchCurrency): CurrencyCommand? {
        return loadCurrency(request)?.awaitFirstOrNull()?.toCommand()
    }

    override suspend fun fetchAllCurrenciesPrecision(): List<CurrencyPrecision> {
        return currencyRepository.fetchAllCurrenciesPrecision().collectList().awaitFirstOrElse { emptyList() }
    }

    private suspend fun loadCurrency(request: FetchCurrency): Mono<CurrencyModel>? {
        if (request.uuid == null && request.symbol == null)
            throw OpexError.BadRequest.exception()
        return currencyRepository.fetchCurrency(symbol = request.symbol, uuid = request.uuid)
    }

    private suspend fun loadCurrencies(request: FetchCurrency?): Flux<CurrencyModel>? {
        return currencyRepository.fetchSemiCurrencies(request?.symbol, request?.name)
    }

    private suspend fun doSave(request: CurrencyModel): CurrencyModel? {
        return currencyRepository.save(request).awaitFirstOrNull()
    }

    private suspend fun doPersist(request: CurrencyModel): CurrencyModel? {
        with(request) {

            currencyRepository.insert(
                this.symbol,
                this.uuid!!,
                this.name,
                this.precision,
                this.title,
                this.alias,
                this.icon,
                this.isTransitive,
                this.isActive,
                this.sign,
                this.description,
                this.shortDescription,
                this.externalUrl,
                this.order
            ).awaitFirstOrNull()

        }
        return currencyRepository.fetchCurrency(uuid = request.uuid)?.awaitFirstOrNull()
    }


}