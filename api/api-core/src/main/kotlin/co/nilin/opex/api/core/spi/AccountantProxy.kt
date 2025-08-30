package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.UserFee

interface AccountantProxy {

    fun getPairConfigs(): List<PairConfigResponse>

    fun getFeeConfigs(): List<FeeConfig>

    fun getUserFee(uuid: String): UserFee
}