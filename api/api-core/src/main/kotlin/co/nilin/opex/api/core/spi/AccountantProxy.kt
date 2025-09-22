package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.common.utils.Interval
import java.math.BigDecimal

interface AccountantProxy {

    fun getPairConfigs(): List<PairConfigResponse>

    fun getFeeConfigs(): List<FeeConfig>

    fun getUserFee(uuid: String): UserFee

    fun getTradeVolumeByCurrency(uuid: String, symbol: String, interval: Interval): BigDecimal

    fun getTotalTradeVolumeValue(uuid: String, interval: Interval): BigDecimal
}