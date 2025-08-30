package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserCurrencyVolume
import co.nilin.opex.api.core.inout.UserTotalVolumeValue
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/user/data")
class UserDataController(private val proxy: MarketUserDataProxy) {

    @GetMapping("/trade/volume")
    suspend fun getTradeVolumeByCurrency(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam symbol: String,
        @RequestParam interval: Interval
    ): UserCurrencyVolume {
        checkValidInterval(interval)
        val uuid = securityContext.authentication.name
        return proxy.getTradeVolumeByCurrency(uuid, symbol, interval)
    }

    @GetMapping("/trade/volume/total")
    suspend fun getTotalTradeVolumeValue(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam interval: Interval
    ): UserTotalVolumeValue {
        checkValidInterval(interval)
        val uuid = securityContext.authentication.name
        return proxy.getTotalTradeVolumeValue(uuid, interval)
    }

    private fun checkValidInterval(interval: Interval) {
        if (interval == Interval.Day || interval == Interval.Week || interval == Interval.Month || interval == Interval.Year)
            return
        throw OpexError.BadRequest.exception()
    }
}