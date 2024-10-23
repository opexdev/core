package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.SwapResponse
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
class AdvancedTransferController {

    @Autowired
    lateinit var transferService: TransferService

    @Autowired
    lateinit var reservedTransferManager: ReservedTransferManager

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
        @PathVariable symbol: String,
        @PathVariable amount: BigDecimal,
        @PathVariable destSymbol: String,
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
            request.sourceAmount,
            request.sourceSymbol,
            request.destSymbol,
            request.senderUuid!!,
            WalletType.MAIN,
            request.receiverUuid,
            WalletType.MAIN
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
        @PathVariable reserveUuid: String,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @CurrentSecurityContext securityContext: SecurityContext?
    ): TransferResult {
        return transferService.advanceTransfer(
            reserveUuid,
            description,
            transferRef,
            securityContext?.authentication?.name
        )
    }

    @PostMapping("/v1/swap/history")
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
    suspend fun getSwapHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest

    ): List<SwapResponse>? {
        return with(request) {
            reservedTransferManager.findByCriteria(
                securityContext.authentication.name,
                sourceSymbol,
                destSymbol,
                startTime?.let {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    )
                },
                endTime?.let {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    )
                },
                limit ?: 10,
                offset ?: 0,
                ascendingByTime,
                status
            )
        }
    }

}