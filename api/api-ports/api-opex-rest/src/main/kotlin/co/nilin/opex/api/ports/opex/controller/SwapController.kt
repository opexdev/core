package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ReservedTransferResponse
import co.nilin.opex.api.core.inout.TransferReserveRequest
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/swap")
@Tag(name = "Swap", description = "Authenticated swap reserve and finalize operations.\n\nAllowed values:\n- sourceWalletType and destWalletType where used: MAIN, EXCHANGE, CASHOUT.\nSource of values:\n- sourceSymbol and destSymbol are server-provided currency symbols.")
class SwapController(
    val walletProxy: WalletProxy
) {
    @PostMapping("/reserve")
    @Operation(
        summary = "Reserve",
        description = """POST /opex/v1/swap/reserve.
Security: Bearer user-token required. Requires authenticated user JWT.
Source of values:
- sourceSymbol and destSymbol are server-provided currency symbols.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ReservedTransferResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun reserve(
        @RequestBody request: TransferReserveRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReservedTransferResponse {
        return walletProxy.reserveSwap(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/finalize/{reserveUuid}")
    @Operation(
        summary = "Finalize transfer",
        description = """POST /opex/v1/swap/finalize/{reserveUuid}.
Behavior: `description` and `transferRef` are optional metadata for finalizing a reserved swap.
Security: Bearer user-token required. Requires authenticated user JWT.
Source of values:
- sourceSymbol and destSymbol are server-provided currency symbols.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TransferResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun finalizeTransfer(
        @Parameter(name = "reserveUuid", description = "Swap reserve UUID returned by reserve endpoint.", required = true)
        @PathVariable reserveUuid: String,
        @Parameter(name = "description", description = "Optional transfer description.", required = false)
        @RequestParam description: String?,
        @Parameter(name = "transferRef", description = "Optional external transfer reference.", required = false)
        @RequestParam transferRef: String?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.finalizeSwap(
            securityContext.jwtAuthentication().tokenValue(),
            reserveUuid,
            description,
            transferRef
        )
    }
}
