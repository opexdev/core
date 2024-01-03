package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.core.service.otc.RateService
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dao.ForbiddenPairRepository
import co.nilin.opex.wallet.ports.postgres.dao.RatesRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.ForbiddenPairModel
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class RateServiceImpl(
    private val ratesRepository: RatesRepository,
    private val forbiddenPairRepository: ForbiddenPairRepository,
    private val currencyRepository: CurrencyRepository
) : RateService {

    override suspend fun addRate(rate: Rate) {
        rate.isValid()
        ratesRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
            ?.let {
                throw OpexError.PairIsExist.exception()
            } ?: run {
            forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
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
        return ratesRepository.findBySourceSymbolAndDestinationSymbol(sourceSymbol, destinationSymbol)
            ?.awaitFirstOrNull()
            ?.toDto() ?: throw OpexError.PairNotFound.exception()
    }

    override suspend fun deleteRate(rate: Rate): Rates {
        return Rates(ratesRepository
            .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()?.let {
                ratesRepository.deleteBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull().let {
                    ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                }
            } ?: throw OpexError.PairNotFound.exception())
    }

    override suspend fun updateRate(rate: Rate): Rates {

        return Rates(ratesRepository
            .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()?.let { it ->
                ratesRepository.save(RateModel(it.id, rate.sourceSymbol, rate.destSymbol, rate.rate, LocalDateTime.now(), it.createDate))?.awaitFirstOrNull().let {
                    ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                }
            }
            ?: throw OpexError.PairNotFound.exception())
    }

    override suspend fun addForbiddenPair(forbiddenPair: ForbiddenPair) {
        forbiddenPair.isValid()
        forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull()?.let {
            throw OpexError.PairIsExist.exception()
        } ?: forbiddenPairRepository.save(forbiddenPair.toModel()).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository
            .findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull()?.let {
                forbiddenPairRepository.deleteBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull().let {
                    forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull()
                }
            } ?: throw OpexError.PairNotFound.exception())
    }


    override suspend fun getForbiddenPairs(): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }

    override suspend fun addTransitiveSymbols(symbols: Symbols) {

        symbols.symbols?.forEach {
            currencyRepository.findBySymbol(it)?.awaitFirstOrNull()?.let {
                if (it.isActive == true)
                    currencyRepository.save(it.apply { isTransitive = true }).awaitFirstOrNull()
            }
        }

    }

    override suspend fun deleteTransitiveSymbols(symbols: Symbols): Symbols {
        symbols.symbols?.forEach {
            currencyRepository.findBySymbol(it)?.awaitFirstOrNull()?.let {
                currencyRepository.save(it.apply { isTransitive = false }).awaitFirstOrNull()
            }
        }
        return Symbols(currencyRepository.findByIsTransitive(true)?.map(CurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun getTransitiveSymbols(): Symbols {
        return Symbols(currencyRepository.findByIsTransitive(true)?.map(CurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
    }


    private fun Rate.toModel(): RateModel {
        return RateModel(
            null,
            this.sourceSymbol,
            this.destSymbol,
            this.rate,
            LocalDateTime.now(),
            LocalDateTime.now()
        )

    }

    private fun RateModel.toDto(): Rate {
        return Rate(

            this.sourceSymbol,
            this.destinationSymbol,
            this.rate,

            )

    }

    private fun ForbiddenPair.toModel(): ForbiddenPairModel {
        return ForbiddenPairModel(
            null,
            this.sourceSymbol,
            this.destSymbol,
            LocalDateTime.now(),
            LocalDateTime.now()
        )

    }

    private fun ForbiddenPairModel.toDto(): ForbiddenPair {
        return ForbiddenPair(
            this.sourceSymbol,
            this.destinationSymbol,

            )

    }

    private suspend fun Rate.isValid() {
        /*        val transitives = getTransitiveSymbols().symbols
                //TODO it's not a valid assumption, it's possible to add direct rate between two non transitive symbol, in this case this rate has priority over indirect rate
                if (!(transitives?.contains(this.sourceSymbol) == true || transitives?.contains(this.destSymbol) == true))
                    throw OpexException(OpexError.AtLeastNeedOneTransitiveSymbol)*/

        currencyRepository.findBySymbol(this.sourceSymbol)?.awaitFirstOrNull()?.let { it ->
            if (it.isActive == false)
                throw OpexError.CurrencyIsDisable.exception()
            currencyRepository.findBySymbol(this.destSymbol)?.awaitFirstOrNull()?.let {
                if (it.isActive == false)
                    throw OpexError.CurrencyIsDisable.exception()
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

    private suspend fun ForbiddenPair.isValid() {
        currencyRepository.findBySymbol(this.sourceSymbol)?.awaitFirstOrNull()?.let {
            currencyRepository.findBySymbol(this.destSymbol)?.awaitFirstOrNull()?.let {
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

}