package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.core.service.otc.RateService
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.ForbiddenPairRepository
import co.nilin.opex.wallet.ports.postgres.dao.ForbiddenSwapPairRepository
import co.nilin.opex.wallet.ports.postgres.dao.RatesRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import co.nilin.opex.wallet.ports.postgres.model.ForbiddenPairModel
import co.nilin.opex.wallet.ports.postgres.model.ForbiddenSwapPairModel
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class RateServiceImpl(
    private val ratesRepository: RatesRepository,
    private val forbiddenPairRepository: ForbiddenPairRepository,
    private val forbiddenSwapPairRepository: ForbiddenSwapPairRepository,
    private val currencyRepository: CurrencyRepositoryV2
) : RateService {

    private val logger = LoggerFactory.getLogger(RateServiceImpl::class.java)


    override suspend fun addRate(rate: Rate, ignoreIfExist: Boolean?, applicator: String? = null) {
        rate.isValid()
        ratesRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
            ?.let {
                if (!ignoreIfExist!!)
                    throw OpexError.PairIsExist.exception()
            } ?: run {
            forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)
                ?.awaitFirstOrNull()
        }?.let {
            throw OpexError.ForbiddenPair.exception()
        } ?: run {
            ratesRepository.save(rate.toModel(applicator)).awaitFirstOrNull()
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

    override suspend fun deleteRate(rate: Rate, applicator: String? = null): Rates {
        return Rates(
            ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()?.let {
                    ratesRepository.deleteBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)
                        ?.awaitFirstOrNull().let {
                            ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                        }
                } ?: throw OpexError.PairNotFound.exception()).also {
            logger.info("Admin {} deleted OTC rate {}-{}", applicator, rate.sourceSymbol, rate.destSymbol)
        }
    }

    override suspend fun updateRate(rate: Rate, applicator: String? = null): Rates {
        return Rates(
            ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull()
                ?.let { it ->
                    ratesRepository.save(
                        RateModel(
                            it.id,
                            rate.sourceSymbol,
                            rate.destSymbol,
                            rate.rate,
                            LocalDateTime.now(),
                            it.createDate,
                            applicator
                        )
                    )?.awaitFirstOrNull().let {
                        ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull()
                    }
                }
                ?: throw OpexError.PairNotFound.exception())
    }

    override suspend fun upsertRate(rate: Rate): Rate {
        try {
            rate.isValid()
        val existing = ratesRepository
            .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)
            ?.awaitFirstOrNull()

        if (existing != null) {
            ratesRepository.save(
                RateModel(
                    existing.id,
                    rate.sourceSymbol,
                    rate.destSymbol,
                    rate.rate,
                    LocalDateTime.now(),
                    existing.createDate
                )
            ).awaitFirstOrNull()
        } else {
            forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)
                ?.awaitFirstOrNull()?.let {
                    throw OpexError.ForbiddenPair.exception()
                }
            ratesRepository.save(rate.toModel()).awaitFirstOrNull()
        }
        } catch (e: Exception) {
            logger.error(
                "Failed to upsert rate for ${rate.sourceSymbol}-${rate.destSymbol} with value ${rate.rate}"
            )
        }
        return rate
    }

    override suspend fun addForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String? = null) {
        isPairValid(forbiddenPair.sourceSymbol, forbiddenPair.destinationSymbol)
        forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(
            forbiddenPair.sourceSymbol,
            forbiddenPair.destinationSymbol
        )?.awaitFirstOrNull()?.let {
            throw OpexError.PairIsExist.exception()
        } ?: forbiddenPairRepository.save(forbiddenPair.toModel(applicator)).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String? = null): ForbiddenPairs {
        return ForbiddenPairs(
            forbiddenPairRepository
                .findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destinationSymbol)
                ?.awaitFirstOrNull()?.let {
                    forbiddenPairRepository.deleteBySourceSymbolAndDestinationSymbol(
                        forbiddenPair.sourceSymbol,
                        forbiddenPair.destinationSymbol
                    )?.awaitFirstOrNull().let {
                        forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())
                            ?.awaitFirstOrNull()
                    }
                } ?: throw OpexError.PairNotFound.exception()).also {
            logger.info("Admin {} deleted forbidden pair {}-{}", applicator, forbiddenPair.sourceSymbol, forbiddenPair.destinationSymbol)
        }
    }


    override suspend fun getForbiddenPairs(): ForbiddenPairs {
        return ForbiddenPairs(
            forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull()
        )
    }

    override suspend fun addForbiddenSwapPair(forbiddenSwapPair: ForbiddenSwapPair, applicator: String? = null) {
        isPairValid(forbiddenSwapPair.sourceSymbol, forbiddenSwapPair.destinationSymbol)
        forbiddenSwapPairRepository.findBySourceSymbolAndDestinationSymbol(
            forbiddenSwapPair.sourceSymbol,
            forbiddenSwapPair.destinationSymbol
        )?.awaitFirstOrNull()?.let {
            throw OpexError.PairIsExist.exception()
        } ?: forbiddenSwapPairRepository.save(forbiddenSwapPair.toModel(applicator)).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenSwapPair(forbiddenSwapPair: ForbiddenSwapPair, applicator: String? = null): ForbiddenSwapPairs {
        return ForbiddenSwapPairs(
            forbiddenSwapPairRepository
                .findBySourceSymbolAndDestinationSymbol(
                    forbiddenSwapPair.sourceSymbol,
                    forbiddenSwapPair.destinationSymbol
                )
                ?.awaitFirstOrNull()?.let {
                    forbiddenSwapPairRepository.deleteBySourceSymbolAndDestinationSymbol(
                        forbiddenSwapPair.sourceSymbol,
                        forbiddenSwapPair.destinationSymbol
                    )?.awaitFirstOrNull().let {
                        forbiddenSwapPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())
                            ?.awaitFirstOrNull()
                    }
                } ?: throw OpexError.PairNotFound.exception()).also {
            logger.info("Admin {} deleted forbidden swap pair {}-{}", applicator, forbiddenSwapPair.sourceSymbol, forbiddenSwapPair.destinationSymbol)
        }
    }

    override suspend fun getForbiddenSwapPairs(): ForbiddenSwapPairs {
        return ForbiddenSwapPairs(
            forbiddenSwapPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())
                ?.awaitFirstOrNull()
        )
    }

    override suspend fun addTransitiveSymbols(symbols: Symbols, applicator: String? = null) {
        symbols.symbols?.forEach {
            currencyRepository.fetchCurrency(symbol = it)?.awaitFirstOrNull()?.let {
                if (it.isActive == true)
                    currencyRepository.save(it.apply {
                        isTransitive = true
                        this.applicator = applicator
                    }.toModel()).awaitFirstOrNull()
            }
        }
    }

    override suspend fun deleteTransitiveSymbols(symbols: Symbols, applicator: String? = null): Symbols {
        symbols.symbols?.forEach {
            currencyRepository.fetchCurrency(symbol = it)?.awaitFirstOrNull()?.let {
                currencyRepository.save(it.apply { isTransitive = false }.toModel()).awaitFirstOrNull()
            }
        }
        return Symbols(
            currencyRepository.findByIsTransitive(true)?.map(CurrencyModel::symbol)?.collect(Collectors.toList())
                ?.awaitFirstOrNull()
        ).also { logger.info("Admin {} deleted transitive symbols {}", applicator, symbols.symbols) }
    }

    override suspend fun getTransitiveSymbols(): Symbols {
        return Symbols(
            currencyRepository.findByIsTransitive(true)?.map(CurrencyModel::symbol)?.collect(Collectors.toList())
                ?.awaitFirstOrNull()
        )
    }

    override suspend fun findStaleRates(updatedBefore: LocalDateTime): List<StaleRate> {
        return ratesRepository.findStaleRates(updatedBefore)
            .collect(Collectors.toList())
            .awaitFirstOrNull()
            ?: emptyList()
    }

    private fun Rate.toModel(applicator: String? = null): RateModel {
        return RateModel(
            null,
            this.sourceSymbol,
            this.destSymbol,
            this.rate,
            LocalDateTime.now(),
            LocalDateTime.now(),
            applicator
        )

    }

    private fun RateModel.toDto(): Rate {
        return Rate(
            this.sourceSymbol,
            this.destinationSymbol,
            this.rate,
            this.applicator,
        )

    }

    private fun ForbiddenPair.toModel(applicator: String? = null): ForbiddenPairModel {
        return ForbiddenPairModel(
            null,
            sourceSymbol,
            destinationSymbol,
            LocalDateTime.now(),
            LocalDateTime.now(),
            applicator
        )

    }

    private fun ForbiddenPairModel.toDto(): ForbiddenPair {
        return ForbiddenPair(sourceSymbol, destinationSymbol, applicator)
    }

    private fun ForbiddenSwapPair.toModel(applicator: String? = null): ForbiddenSwapPairModel {
        return ForbiddenSwapPairModel(
            null,
            sourceSymbol,
            destinationSymbol,
            LocalDateTime.now(),
            LocalDateTime.now(),
            applicator
        )

    }

    private fun ForbiddenSwapPairModel.toDto(): ForbiddenSwapPair {
        return ForbiddenSwapPair(sourceSymbol, destinationSymbol, applicator)
    }

    private suspend fun Rate.isValid() {
        /*        val transitives = getTransitiveSymbols().symbols
                //TODO it's not a valid assumption, it's possible to add direct rate between two non transitive symbol, in this case this rate has priority over indirect rate
                if (!(transitives?.contains(this.sourceSymbol) == true || transitives?.contains(this.destSymbol) == true))
                    throw OpexException(OpexError.AtLeastNeedOneTransitiveSymbol)*/

        currencyRepository.fetchCurrency(symbol = this.sourceSymbol)?.awaitFirstOrNull()?.let { it ->
            if (it.isActive == false)
                throw OpexError.CurrencyIsDisable.exception()
            currencyRepository.fetchCurrency(symbol = this.destSymbol)?.awaitFirstOrNull()?.let {
                if (it.isActive == false)
                    throw OpexError.CurrencyIsDisable.exception()
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

    private suspend fun isPairValid(sourceSymbol: String, destinationSymbol: String) {
        currencyRepository.fetchCurrency(symbol = sourceSymbol)?.awaitFirstOrNull()?.let {
            currencyRepository.fetchCurrency(symbol = destinationSymbol)?.awaitFirstOrNull()?.let {
            } ?: throw OpexError.CurrencyNotFound.exception()
        } ?: throw OpexError.CurrencyNotFound.exception()
    }

}