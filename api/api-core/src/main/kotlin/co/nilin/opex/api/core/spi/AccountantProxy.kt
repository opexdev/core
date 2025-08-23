package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairConfigResponse

interface AccountantProxy {

    fun getPairConfigs(): List<PairConfigResponse>

    fun getFeeConfigs(): List<FeeConfig>

    fun getFeeConfig(symbol: String): PairFeeResponse

}