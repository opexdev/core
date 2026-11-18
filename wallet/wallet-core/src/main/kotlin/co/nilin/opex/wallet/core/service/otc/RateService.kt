package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.*
import java.time.LocalDateTime

interface RateService {

    suspend fun addRate(rate: Rate, ignoreIfExist: Boolean? = false, applicator: String)

    suspend fun deleteRate(rate: Rate, applicator: String): Rates

    suspend fun getRate(): Rates

    suspend fun getRate(sourceSymbol: String, destinationSymbol: String): Rate?

    suspend fun updateRate(rate: Rate, applicator: String): Rates

    suspend fun upsertRate(rate: Rate): Rate

    suspend fun addForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String)

    suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String): ForbiddenPairs

    suspend fun getForbiddenPairs(): ForbiddenPairs

    suspend fun addForbiddenSwapPair(forbiddenPair: ForbiddenSwapPair, applicator: String)

    suspend fun deleteForbiddenSwapPair(forbiddenPair: ForbiddenSwapPair, applicator: String): ForbiddenSwapPairs

    suspend fun getForbiddenSwapPairs(): ForbiddenSwapPairs

    suspend fun addTransitiveSymbols(symbols: Symbols, applicator: String)

    suspend fun deleteTransitiveSymbols(symbols: Symbols, applicator: String): Symbols

    suspend fun getTransitiveSymbols(): Symbols

    suspend fun findStaleRates(updatedBefore: LocalDateTime): List<StaleRate>

}