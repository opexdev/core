package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.dto.TransferPreEvaluateResponse
import co.nilin.opex.wallet.app.dto.TransferReserveRequest
import co.nilin.opex.wallet.app.dto.TransferReserveResponse
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.TransferResult
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class AdvancedTransferController {

    @Autowired
    lateinit var transferService: TransferService

    @GetMapping("/v3/amount/{amount}_{symbol}/{destSymbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{ \"destAmount\": \"111\"}",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun calculateDestinationAmount(
            @PathVariable("symbol") symbol: String,
            @PathVariable("amount") amount: BigDecimal,
            @PathVariable("destSymbol") destSymbol: String,
    ): TransferPreEvaluateResponse {
        return TransferPreEvaluateResponse(transferService.calculateDestinationAmount(symbol, amount, destSymbol))
    }

    @PostMapping("/v3/transfer/reserve")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{ \"reserveUuid\": \"214234\"," +
                                    "  \"guaranteedDestAmount\": \"1000\"}",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun reserve(
            @RequestBody request: TransferReserveRequest,
            @CurrentSecurityContext securityContext: SecurityContext?
    ): ReservedTransferResponse {
        securityContext?.let {
            if (request.senderUuid != it.authentication.name)
                throw OpexError.Forbidden.exception()
            request.senderUuid = it.authentication.name
        }
        return transferService.reserveTransfer(
                request.sourceAmount, request.sourceSymbol, request.destSymbol, request.senderUuid!!, request.senderWalletType, request.receiverUuid, request.receiverWalletType
        )

    }

    @PostMapping("/v3/transfer/{reserveUuid}")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{}",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun finalizeTransfer(
            @PathVariable("reserveUuid") reserveUuid: String,
            @RequestParam("description") description: String?,
            @RequestParam("transferRef") transferRef: String?,
            @CurrentSecurityContext securityContext: SecurityContext?
    ): TransferResult {
        return transferService.advanceTransfer(reserveUuid, description, transferRef, securityContext?.authentication?.name)
    }
}