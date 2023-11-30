package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.*

interface GraphService {

    suspend fun addRate(rate: Rate)

    suspend fun deleteRate(rate: Rate): Rates

    suspend fun getRates():Rates

    suspend fun getRates(sourceSymbol:String,destinationSymbol:String):Rate?


    suspend fun updateRate(rate: Rate):Rates

    suspend fun addForbiddenPair(forbiddenPair:ForbiddenPair)

    suspend fun deleteForbiddenPair(forbiddenPair:ForbiddenPair):ForbiddenPairs

    suspend fun getForbiddenPairs():ForbiddenPairs


    suspend fun addTransitiveSymbols(symbols:Symbols)

    suspend fun deleteTransitiveSymbols(symbols:Symbols):Symbols

    suspend fun getTransitiveSymbols():Symbols


}