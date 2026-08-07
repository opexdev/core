package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AdminTransferReserveRequest
import co.nilin.opex.api.core.inout.ReservedTransferResponse
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/swap")
@Tag(name = "Admin Swap", description = "Admin impersonated swap reserve and finalize operations.")
class AdminSwapController(
    val walletProxy: WalletProxy
) {
    @PostMapping("/reserve")
    @Operation(
        summary = "Admin Reserve Swap",
        description = """POST /opex/v1/admin/swap/reserve.
Security: Bearer admin-token required. Requires authenticated admin JWT.
Behavior: Admin can reserve swap on behalf of a user by specifying custom rate or amounts.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReservedTransferResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. User does not have admin permissions.",
                content = [Content()]
            )
        ]
    )
    suspend fun reserve(
        @RequestBody request: AdminTransferReserveRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReservedTransferResponse {
        return walletProxy.reserveSwapByAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/finalize/{reserveUuid}")
    @Operation(
        summary = "Admin Finalize Transfer",
        description = """POST /opex/v1/admin/swap/finalize/{reserveUuid}.
Security: Bearer admin-token required. Requires authenticated admin JWT.
Behavior: Finalizes a reserved swap on behalf of the user.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TransferResult::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. User does not have admin permissions.",
                content = [Content()]
            )
        ]
    )
    suspend fun finalizeTransfer(
        @Parameter(
            name = "reserveUuid",
            description = "Swap reserve UUID returned by reserve endpoint.",
            required = true
        )
        @PathVariable reserveUuid: String,
        @Parameter(name = "description", description = "Optional transfer description.", required = false)
        @RequestParam description: String?,
        @Parameter(name = "transferRef", description = "Optional external transfer reference.", required = false)
        @RequestParam transferRef: String?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.finalizeSwapByAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            reserveUuid,
            description,
            transferRef
        )
    }
}