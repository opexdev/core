package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.PairFeeResponse
import co.nilin.opex.market.core.inout.PairInfoResponse

interface AccountantProxy {

    suspend fun getPairConfigs(): List<PairInfoResponse>

    suspend fun getFeeConfigs(): List<PairFeeResponse>

}