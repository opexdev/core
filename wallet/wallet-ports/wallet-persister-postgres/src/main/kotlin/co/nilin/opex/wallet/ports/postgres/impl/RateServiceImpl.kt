package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
import co.nilin.opex.wallet.core.model.otc.ForbiddenPairs
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.Rates
import co.nilin.opex.wallet.core.service.otc.GraphService
import co.nilin.opex.wallet.ports.postgres.dao.ForbiddenPairRepository
import co.nilin.opex.wallet.ports.postgres.dao.RatesRepository
import co.nilin.opex.wallet.ports.postgres.model.otc.ForbiddenPairModel
import co.nilin.opex.wallet.ports.postgres.model.otc.RateModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class RateServiceImpl(private val ratesRepository: RatesRepository,
                      private val forbiddenPairRepository: ForbiddenPairRepository) : GraphService {

    override suspend fun addRate(rate: Rate) {
        ratesRepository.findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)
                ?.let {
                    throw OpexException(OpexError.PairIsExist)
                } ?: run {
            forbiddenPairRepository.findAllBy()?.filter {
                it.sourceSymbol == rate.sourceSymbol && it.destinationSymbol == rate.destSymbol
            }?.let {
                throw OpexException(OpexError.ForbiddenPair)
            } ?: run {
                ratesRepository.save(rate.toModel())?.awaitFirstOrNull()
            }
            ratesRepository.save(rate.toModel()).awaitFirstOrNull()
        }
    }


    override suspend fun getRates(): Rates {
        return Rates(ratesRepository.findAll().map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
    }


    override suspend fun getRates(sourceSymbol:String, destinationSymbol:String): Rates {
        return Rates(ratesRepository.findRoutes(sourceSymbol,destinationSymbol)?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull())
    }

    override suspend fun deleteRate(rate: Rate): Rates {
        return Rates(ratesRepository
                .findBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.let {
                    ratesRepository.deleteBySourceSymbolAndDestinationSymbol(rate.sourceSymbol, rate.destSymbol)?.awaitFirstOrNull().let {
                        ratesRepository.findAll()?.map { it.toDto() }?.collect(Collectors.toList()).awaitFirstOrNull()
                    }
                            ?: throw OpexException(OpexError.PairIsExist)

                })
    }

    override suspend fun updateRate(rate: Rate) {

    }


    override suspend fun addForbiddenPair(forbiddenRate: ForbiddenPair) {
        forbiddenPairRepository.findBySourceSymbolAndDestinationSymbol(forbiddenRate.sourceSymbol, forbiddenRate.destSymbol)?.let {
            throw OpexException(OpexError.PairIsExist)
        } ?: forbiddenPairRepository.save(forbiddenRate.toModel()).awaitFirstOrNull()
    }

    override suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair): ForbiddenPairs {
        return ForbiddenPairs(forbiddenPairRepository
                .findBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.let {
                    forbiddenPairRepository.deleteBySourceSymbolAndDestinationSymbol(forbiddenPair.sourceSymbol, forbiddenPair.destSymbol)?.awaitFirstOrNull().let {
                        forbiddenPairRepository.findAllBy()?.map { it.toDto() }?.collect(Collectors.toList())?.awaitFirstOrNull()
                    }
                            ?: throw OpexException(OpexError.PairIsExist)
                })
    }


    override suspend fun getForbiddenPairs(): ForbiddenPairs{

        return ForbiddenPairs(forbiddenPairRepository.findAll()?.map { it.toDto() }.collect(Collectors.toList()).awaitFirstOrNull())
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
}