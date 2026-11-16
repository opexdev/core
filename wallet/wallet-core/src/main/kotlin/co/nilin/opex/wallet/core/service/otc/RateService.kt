package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.*
import java.time.LocalDateTime

interface RateService {

    suspend fun addRate(rate: Rate, ignoreIfExist: Boolean? = false, applicator: String? = null)

    suspend fun deleteRate(rate: Rate, applicator: String? = null): Rates

    suspend fun getRate(): Rates

    suspend fun getRate(sourceSymbol: String, destinationSymbol: String): Rate?

    suspend fun updateRate(rate: Rate, applicator: String? = null): Rates

    suspend fun upsertRate(rate: Rate): Rate

    suspend fun addForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String? = null)

    suspend fun deleteForbiddenPair(forbiddenPair: ForbiddenPair, applicator: String? = null): ForbiddenPairs

    suspend fun getForbiddenPairs(): ForbiddenPairs

    suspend fun addForbiddenSwapPair(forbiddenPair: ForbiddenSwapPair, applicator: String? = null)

    suspend fun deleteForbiddenSwapPair(forbiddenPair: ForbiddenSwapPair, applicator: String? = null): ForbiddenSwapPairs

    suspend fun getForbiddenSwapPairs(): ForbiddenSwapPairs

    suspend fun addTransitiveSymbols(symbols: Symbols, applicator: String? = null)

    suspend fun deleteTransitiveSymbols(symbols: Symbols, applicator: String? = null): Symbols

    suspend fun getTransitiveSymbols(): Symbols

    suspend fun findStaleRates(updatedBefore: LocalDateTime): List<StaleRate>

}