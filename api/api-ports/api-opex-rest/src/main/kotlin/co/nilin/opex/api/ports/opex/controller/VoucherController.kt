package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.SubmitVoucherResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
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
@RequestMapping("/opex/v1/voucher")
@Tag(name = "Voucher", description = "Authenticated voucher submission operations.")
class VoucherController(private val walletProxy: WalletProxy) {

    @PutMapping("/{code}")
    @Operation(
        summary = "Submit voucher",
        description = """PUT /opex/v1/voucher/{code}.
Security: Bearer user-token required. Required authority: PERM_voucher:submit.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = SubmitVoucherResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: PERM_voucher:submit. No response body.", content = [Content()])
        ]
    )
    suspend fun submitVoucher(
        @Parameter(name = "code", description = "Voucher code.", required = true)
        @PathVariable code: String,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): SubmitVoucherResponse {
        return walletProxy.submitVoucher(code, securityContext.jwtAuthentication().tokenValue())
    }
}
