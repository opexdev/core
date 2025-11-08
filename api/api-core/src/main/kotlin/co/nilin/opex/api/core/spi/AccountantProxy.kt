package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.FeeConfig
import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.inout.WithdrawLimitConfig
import co.nilin.opex.common.utils.Interval
import java.math.BigDecimal

interface AccountantProxy {

    suspend fun getPairConfigs(): List<PairConfigResponse>

    suspend fun getFeeConfigs(): List<FeeConfig>

    suspend fun getUserFee(uuid: String): UserFee

    suspend fun getTradeVolumeByCurrency(uuid: String, symbol: String, interval: Interval): BigDecimal

    suspend fun getTotalTradeVolumeValue(uuid: String, interval: Interval): BigDecimal

    suspend fun getWithdrawLimitConfigs(): List<WithdrawLimitConfig>

    suspend fun getTotalWithdrawVolumeValue(uuid: String, interval: Interval?): BigDecimal
}