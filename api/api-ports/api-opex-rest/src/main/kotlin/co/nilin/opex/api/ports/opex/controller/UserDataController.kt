package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/user/data")
class UserDataController(
    private val accountantProxy: AccountantProxy,
) {

    @GetMapping("/trade/volume")
    suspend fun getTradeVolumeByCurrency(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam symbol: String,
        @RequestParam interval: Interval
    ): BigDecimal {
        checkValidInterval(interval)
        val uuid = securityContext.authentication.name
        return accountantProxy.getTradeVolumeByCurrency(uuid, symbol, interval)
    }

    @GetMapping("/trade/volume/total")
    suspend fun getTotalTradeVolumeValue(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam interval: Interval
    ): BigDecimal {
        checkValidInterval(interval)
        val uuid = securityContext.authentication.name
        return accountantProxy.getTotalTradeVolumeValue(uuid, interval)
    }

    @GetMapping("/fee")
    fun getUserFee(@CurrentSecurityContext securityContext: SecurityContext): UserFee {
        return accountantProxy.getUserFee(securityContext.authentication.name)
    }

    @GetMapping("/withdraw/volume/total")
    suspend fun getTotalWithdrawVolumeValue(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam(required = false) interval: Interval?
    ): BigDecimal {
        val uuid = securityContext.authentication.name
        return accountantProxy.getTotalWithdrawVolumeValue(uuid, interval)
    }

    private fun checkValidInterval(interval: Interval) {
        if (interval == Interval.Day || interval == Interval.Week || interval == Interval.Month || interval == Interval.Year)
            return
        throw OpexError.BadRequest.exception()
    }
}