package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ManualTransferRequest
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/admin/deposit")
@Tag(name = "Deposit Admin", description = "Admin manual deposit operations.")
class DepositAdminController(
    private val walletProxy: WalletProxy
) {
    @PostMapping("/manually/{amount}_{symbol}/{receiverUuid}")
    @Operation(
        summary = "Deposit manually",
        description = """POST /opex/v1/admin/deposit/manually/{amount}_{symbol}/{receiverUuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TransferResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
        ]
    )
    suspend fun depositManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("receiverUuid") receiverUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.depositManually(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            receiverUuid,
            amount,
            request
        )
    }

}
