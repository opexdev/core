package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserCurrencyVolume
import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.inout.UserTotalVolumeValue
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/user/data")
class UserDataController(
    private val proxy: MarketUserDataProxy,
    private val accountantProxy: AccountantProxy,
) {

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

    @GetMapping("/fee")
    fun getUserFee(@CurrentSecurityContext securityContext: SecurityContext): UserFee {
        return accountantProxy.getUserFee(securityContext.authentication.name)
    }

    private fun checkValidInterval(interval: Interval) {
        if (interval == Interval.Day || interval == Interval.Week || interval == Interval.Month || interval == Interval.Year)
            return
        throw OpexError.BadRequest.exception()
    }
}