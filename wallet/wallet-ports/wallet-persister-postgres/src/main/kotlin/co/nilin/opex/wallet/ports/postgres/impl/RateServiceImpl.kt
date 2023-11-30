package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.core.service.otc.GraphService
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
class RateServiceImpl(private val ratesRepository: RatesRepository,
                      private val forbiddenPairRepository: ForbiddenPairRepository,
                      private val currencyRepository: CurrencyRepository) : GraphService {

    override suspend fun addRate(rate: Rate) {
        rate.isValid()
        ratesRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
                ?.let {
                    throw OpexException(OpexError.PairIsExist)
                } ?: run {
            forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
        }?.let {
            throw OpexException(OpexError.ForbiddenPair)
        } ?: run {
            ratesRepository.save(rate.toModel())?.awaitFirstOrNull()
        }

    }


    override suspend fun getRates(): Rates {
        return Rates(ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }


    override suspend fun getRates(sourceSymbol: String, destinationSymbol: String): Rate? {
        return ratesRepository.findBySourceSymbolAndDestinationSymbol(sourceSymbol, destinationSymbol)?.awaitFirstOrNull()
                ?.let { it.toDto() } ?: throw OpexException(OpexError.PairNotFound)
    }

    override suspend fun deleteRate(rate: Rate): Rates {
        return Rates(ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()?.let {
                    ratesRepository.deleteBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull().let {
                        ratesRepository.findAll()?.map { it.toDto() }?.collect(Collectors.toList()).awaitFirstOrNull()
                    }


                } ?: throw OpexException(OpexError.PairNotFound))
    }

    override suspend fun updateRate(rate: Rate): Rates {

        return Rates(ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()?.let { it ->
                    ratesRepository.save(RateModel(it.id, rate.sourceSymbol, rate.destSymbol, rate.rate, LocalDateTime.now(), it.createDate))?.awaitFirstOrNull().let {
                        ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList())?.awaitFirstOrNull()
                    }
                }
                ?: throw OpexException(OpexError.PairNotFound))
    }

    override suspend fun addForbiddenPair(forbiddenRate: ForbiddenPair) {
        forbiddenRate.isValid()
        forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(forbiddenRate.sourceSymbol, forbiddenRate.destSymbol)?.awaitFirstOrNull()?.let {
            throw OpexException(OpexError.PairIsExist)
        } ?: forbiddenPairRepository.save(forbiddenRate.toModel()).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository
                .findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull()?.let {
                    forbiddenPairRepository.deleteBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull().let {
                        forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull()
                    }

                } ?: throw OpexException(OpexError.PairNotFound))
    }


    override suspend fun getForbiddenPairs(): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository.findAll()?.map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }

    override suspend fun addTransitiveSymbols(symbols: Symbols) {

        symbols.symbols?.forEach {
            currencyRepository.findBySymbol(it)?.awaitFirstOrNull()?.let {
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
        return Symbols(currencyRepository.findByIsTransitive(true)?.map (CurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun getTransitiveSymbols(): Symbols {
        return Symbols(currencyRepository.findByIsTransitive(true)?.map (CurrencyModel::symbol)?.collect(Collectors.toList())?.awaitFirstOrNull())
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
        currencyRepository.findBySymbol(this.sourceSymbol)?.awaitFirstOrNull()?.let {
            currencyRepository.findBySymbol(this.destSymbol)?.awaitFirstOrNull()?.let {
            } ?: throw OpexException(OpexError.CurrencyNotFound)
        } ?: throw OpexException(OpexError.CurrencyNotFound)
    }

    private suspend fun ForbiddenPair.isValid() {
        currencyRepository.findBySymbol(this.sourceSymbol)?.awaitFirstOrNull()?.let {
            currencyRepository.findBySymbol(this.destSymbol)?.awaitFirstOrNull()?.let {
            } ?: throw OpexException(OpexError.CurrencyNotFound)
        } ?: throw OpexException(OpexError.CurrencyNotFound)
    }

}