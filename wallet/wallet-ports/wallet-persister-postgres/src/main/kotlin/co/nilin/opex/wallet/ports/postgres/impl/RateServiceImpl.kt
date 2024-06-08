package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.core.service.otc.RateService
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.ForbiddenPairRepository
import co.nilin.opex.wallet.ports.postgres.dao.RatesRepository
import co.nilin.opex.wallet.ports.postgres.model.ForbiddenPairModel
import co.nilin.opex.wallet.ports.postgres.model.NewCurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class RateServiceImpl(
        private val ratesRepository: RatesRepository,
        private val forbiddenPairRepository: ForbiddenPairRepository,
        private val currencyRepository: CurrencyRepositoryV2
) : RateService {
    private val logger = LoggerFactory.getLogger(RateServiceImpl::class.java)

    //todo  change currency symbol -> uuid in all interfaces

    override suspend fun addRate(rate: Rate) {
        rate.isValid()
        rate.apply {
            sourceSymbolId=sourceSymbol.currencyMapping()
            destinationSymbolId=destinationSymbol.currencyMapping()
        }
        ratesRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbolId!!, rate.destinationSymbolId!!)?.awaitFirstOrNull()
                ?.let {
                    throw OpexError.PairIsExist.exception()
                } ?: run {
            forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbolId!!, rate.destinationSymbolId!!)?.awaitFirstOrNull()
        }?.let {
            throw OpexError.ForbiddenPair.exception()
        } ?: run {
            ratesRepository.save(rate.toModel()).awaitFirstOrNull()
        }

    }


    override suspend fun getRate(): Rates {
        return Rates(ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }


    override suspend fun getRate(sourceSymbol: String, destinationSymbol: String): Rate? {

        return ratesRepository.findBySourceSymbolAndDestinationSymbol(sourceSymbol.currencyMapping(), destinationSymbol.currencyMapping())
                ?.awaitFirstOrNull()
                ?.toDto() ?: throw OpexError.PairNotFound.exception()
    }

    override suspend fun deleteRate(rate: Rate): Rates {
        rate.apply {
            sourceSymbolId=sourceSymbol.currencyMapping()
            destinationSymbolId=destinationSymbol.currencyMapping()
        }
        return Rates(ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbolId!!, rate.destinationSymbolId!!)?.awaitFirstOrNull()?.let {
                    ratesRepository.deleteBySourceSymbolAndDestinationSymbol(rate.sourceSymbolId!!, rate.destinationSymbolId!!)?.awaitFirstOrNull().let {
                        ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                    }
                } ?: throw OpexError.PairNotFound.exception())
    }

    override suspend fun updateRate(rate: Rate): Rates {
        rate.apply {
            sourceSymbolId=sourceSymbol.currencyMapping()
            destinationSymbolId=destinationSymbol.currencyMapping()
        }
        return Rates(ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbolId!!, rate.destinationSymbolId!!)?.awaitFirstOrNull()?.let { it ->
                    ratesRepository.save(RateModel(it.id, rate.sourceSymbolId!!, rate.destinationSymbolId!!, rate.rate, LocalDateTime.now(), it.createDate))?.awaitFirstOrNull().let {
                        ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                    }
                }
                ?: throw OpexError.PairNotFound.exception())
    }

    override suspend fun addForbiddenPair(forbiddenPair: ForbiddenPair) {
        forbiddenPair.isValid()
        forbiddenPair.apply {
            sourceSymbolId=sourceSymbol.currencyMapping()
            destinationSymbolId=destinationSymbol.currencyMapping()
        }
        forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbolId!!, forbiddenPair.destinationSymbolId!!)?.awaitFirstOrNull()?.let {
            throw OpexError.PairIsExist.exception()
        } ?: forbiddenPairRepository.save(forbiddenPair.toModel()).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair): ForbiddenPairs {
        forbiddenPair.apply {
            sourceSymbolId=sourceSymbol.currencyMapping()
            destinationSymbolId=destinationSymbol.currencyMapping()
        }
        return ForbiddenPairs(forbiddenPairRepository
                .findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbolId!!, forbiddenPair.destinationSymbolId!!)?.awaitFirstOrNull()?.let {
                    forbiddenPairRepository.deleteBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbolId!!, forbiddenPair.destinationSymbolId!!)?.awaitFirstOrNull().let {
                        forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull()
                    }
                } ?: throw OpexError.PairNotFound.exception())
    }


    override suspend fun getForbiddenPairs(): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }

    override suspend fun addTransitiveSymbols(symbols: Symbols) {

        symbols.symbols?.forEach {
            currencyRepository.fetchCurrency(symbol = it)?.awaitFirstOrNull()?.let {
                if (it.isActive == true)
                    currencyRepository.save(it.apply { isTransitive = true }).awaitFirstOrNull()
            }
        }

    }

    override suspend fun deleteTransitiveSymbols(symbols: Symbols): Symbols {
        symbols.symbols?.forEach {
            currencyRepository.fetchCurrency(symbol = it)?.awaitFirstOrNull()?.let {
                currencyRepository.save(it.apply { isTransitive = false }).awaitFirstOrNull()
            }
        }
        return Symbols(currencyRepository.findByIsTransitive(true)?.map(NewCurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun getTransitiveSymbols(): Symbols {
        return Symbols(currencyRepository.findByIsTransitive(true)?.map(NewCurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
    }


    private fun Rate.toModel(): RateModel {
        return RateModel(
                null,
                this.sourceSymbolId!!,
                this.destinationSymbolId!!,
                this.rate,
                LocalDateTime.now(),
                LocalDateTime.now()
        )

    }

    private suspend fun RateModel.toDto(): Rate {
        return Rate(
                this.sourceSymbol.currencyMapping(),
                this.destinationSymbol.currencyMapping(),
                this.rate,
                )

    }

    private fun ForbiddenPair.toModel(): ForbiddenPairModel {
        return ForbiddenPairModel(
                null,
                this.sourceSymbolId!!,
                this.destinationSymbolId!!,
                LocalDateTime.now(),
                LocalDateTime.now()
        )

    }

    private suspend fun ForbiddenPairModel.toDto(): ForbiddenPair {
        return ForbiddenPair(
                this.sourceSymbol.currencyMapping(),
                this.destinationSymbol.currencyMapping(),
                )

    }

    private suspend fun Rate.isValid() {
        /*        val transitives = getTransitiveSymbols().symbols
                //TODO it's not a valid assumption, it's possible to add direct rate between two non transitive symbol, in this case this rate has priority over indirect rate
                if (!(transitives?.contains(this.sourceSymbol) == true || transitives?.contains(this.destSymbol) == true))
                    throw OpexException(OpexError.AtLeastNeedOneTransitiveSymbol)*/

        currencyRepository.fetchCurrency(id = this.sourceSymbol.currencyMapping())?.awaitFirstOrNull()?.let { it ->
            if (it.isActive == false)
                throw OpexError.CurrencyIsDisable.exception()
            currencyRepository.fetchCurrency(id = this.destinationSymbol.currencyMapping())?.awaitFirstOrNull()?.let {
                if (it.isActive == false)
                    throw OpexError.CurrencyIsDisable.exception()
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

    private suspend fun ForbiddenPair.isValid() {
        currencyRepository.fetchCurrency(symbol = this.sourceSymbol)?.awaitFirstOrNull()?.let {
            currencyRepository.fetchCurrency(symbol = this.destinationSymbol)?.awaitFirstOrNull()?.let {
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }


    private suspend fun String.currencyMapping(): Long {
        return currencyRepository.fetchCurrency(symbol = this)?.awaitFirstOrNull()?.let { it.id }
                ?: throw OpexError.CurrencyNotFound.exception()
    }

    private suspend fun Long.currencyMapping(): String {
        return currencyRepository.fetchCurrency(id = this)?.awaitFirstOrNull()?.let { it.symbol }
                ?: throw OpexError.CurrencyNotFound.exception()
    }

}