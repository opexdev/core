package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.UserFee
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/user/data")
@Tag(name = "User Data", description = "Authenticated user data endpoints")
class UserDataController(
    private val accountantProxy: AccountantProxy,
) {

    @GetMapping("/trade/volume")
    @Operation(
        summary = "Get user trade volume by currency",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "symbol", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string")),
            Parameter(name = "interval", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string", description = "Interval: Day|Week|Month|Year"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(type = "number", format = "double")) ]) ]
    )
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
    @Operation(
        summary = "Get user total trade volume value",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "interval", `in` = ParameterIn.QUERY, required = true, schema = Schema(type = "string", description = "Interval: Day|Week|Month|Year"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(type = "number", format = "double")) ]) ]
    )
    suspend fun getTotalTradeVolumeValue(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam interval: Interval
    ): BigDecimal {
        checkValidInterval(interval)
        val uuid = securityContext.authentication.name
        return accountantProxy.getTotalTradeVolumeValue(uuid, interval)
    }

    @GetMapping("/fee")
    @Operation(
        summary = "Get user fee settings",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(implementation = UserFee::class)) ]) ]
    )
    suspend fun getUserFee(@CurrentSecurityContext securityContext: SecurityContext): UserFee {
        return accountantProxy.getUserFee(securityContext.authentication.name)
    }

    @GetMapping("/withdraw/volume/total")
    @Operation(
        summary = "Get user total withdraw volume value",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "interval", `in` = ParameterIn.QUERY, required = false, schema = Schema(type = "string", description = "Optional label; maps to Interval by label"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = "application/json", schema = Schema(type = "number", format = "double")) ]) ]
    )
    suspend fun getTotalWithdrawVolumeValue(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam(required = false) interval: String?
    ): BigDecimal =
        accountantProxy.getTotalWithdrawVolumeValue(
            securityContext.authentication.name,
            interval?.let(Interval::findByLabel)
        )


    private fun checkValidInterval(interval: Interval) {
        if (interval == Interval.Day || interval == Interval.Week || interval == Interval.Month || interval == Interval.Year)
            return
        throw OpexError.BadRequest.exception()
    }
}