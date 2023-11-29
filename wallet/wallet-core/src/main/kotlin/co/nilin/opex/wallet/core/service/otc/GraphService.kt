package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
import co.nilin.opex.wallet.core.model.otc.ForbiddenPairs
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.Rates

interface GraphService {

    suspend fun addRate(rate: Rate)

    suspend fun deleteRate(rate: Rate): Rates

    suspend fun getRates():Rates

    suspend fun getRates(sourceSymbol:String,destinationSymbol:String):Rates


    suspend fun updateRate(rate: Rate):Rates

    suspend fun addForbiddenPair(forbiddenPair:ForbiddenPair)

    suspend fun deleteForbiddenPair(forbiddenPair:ForbiddenPair):ForbiddenPairs

    suspend fun getForbiddenPairs():ForbiddenPairs


}