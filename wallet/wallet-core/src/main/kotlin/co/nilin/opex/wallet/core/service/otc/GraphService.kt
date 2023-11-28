package co.nilin.opex.wallet.core.service.otc

import co.nilin.opex.wallet.core.model.otc.ForbiddenPair
import co.nilin.opex.wallet.core.model.otc.Rate

interface GraphService {

    suspend fun addRate(rate: Rate)

    suspend fun deleteRate(rate: Rate):List<Rate>?

    suspend fun getRates():List<Rate>?

    suspend fun updateRate(rate: Rate)

    suspend fun addForbiddenPair(forbiddenPair:ForbiddenPair)

    suspend fun deleteForbiddenPair(forbiddenPair:ForbiddenPair):List<ForbiddenPair>?

    suspend fun getForbiddenPairs():List<ForbiddenPair>?


}