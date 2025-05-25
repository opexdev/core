package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairConfigResponse

interface AccountantProxy {

    suspend fun getPairConfigs(): List<PairConfigResponse>

    suspend fun getFeeConfigs(): List<PairFeeResponse>

    suspend fun getFeeConfig(symbol: String): PairFeeResponse

}