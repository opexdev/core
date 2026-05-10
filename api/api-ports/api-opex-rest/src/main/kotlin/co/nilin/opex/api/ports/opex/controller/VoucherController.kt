package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.SubmitVoucherResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/opex/v1/voucher")
@Tag(name = "Voucher", description = "Voucher submission operations")
class VoucherController(private val walletProxy: WalletProxy) {

    @PutMapping("/{code}")
    @Operation(
        summary = "Submit voucher code",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "code", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "string"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = SubmitVoucherResponse::class))
            ])
        ]
    )
    suspend fun submitVoucher(
        @PathVariable code: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): SubmitVoucherResponse {
        return walletProxy.submitVoucher(code, securityContext.jwtAuthentication().tokenValue())
    }
}