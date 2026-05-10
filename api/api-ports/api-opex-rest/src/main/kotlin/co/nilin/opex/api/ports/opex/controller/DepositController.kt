package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.RequestDepositBody
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/deposit")
@Tag(name = "Deposit", description = "Deposit operations")
class DepositController(private val walletProxy: WalletProxy) {

    @PostMapping
    @Operation(
        summary = "Request deposit",
        description = "Submit a deposit request. Security handling is upstream (NEEDS REVIEW).",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = RequestDepositBody::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = TransferResult::class))
            ])
        ]
    )
    suspend fun deposit(@RequestBody request: RequestDepositBody): TransferResult? {
        return walletProxy.deposit(request)
    }
}