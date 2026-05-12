package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.RequestDepositBody
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/deposit")
@Tag(name = "Deposit", description = "Authenticated deposit operations.")
class DepositController(private val walletProxy: WalletProxy) {

    @PostMapping
    @Operation(
        summary = "Deposit",
        description = """POST /opex/v1/deposit.
Security: Bearer user-token required. Required authority: PERM_deposit:write.

Behavior: Use gatewayUuid/chain when the deposit flow requires a specific gateway or network. The receiver wallet type must be one of the server-supported wallet types.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Response body: See schema.", content = [Content(mediaType = "application/json", schema = Schema(implementation = TransferResult::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: PERM_deposit:write. No response body.", content = [Content()])
        ]
    )
    suspend fun deposit(@RequestBody request: RequestDepositBody): TransferResult? {
        return walletProxy.deposit(request)
    }
}
