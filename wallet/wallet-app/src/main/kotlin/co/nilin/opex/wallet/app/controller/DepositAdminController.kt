package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.AdminSearchDepositRequest
import co.nilin.opex.wallet.core.inout.DepositResponse
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.app.service.DepositService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/admin")

class DepositAdminController(private val depositService: DepositService) {


    @PostMapping("/deposit/manually/{amount}_{symbol}/{receiverUuid}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun depositManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("receiverUuid") receiverUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return depositService.depositManually(
            symbol, receiverUuid,
            securityContext.authentication.name, amount, request
        )
    }

    @PostMapping("/deposit/search")
    suspend fun search(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchDepositRequest
    ): List<DepositResponse> {
        return depositService.searchDeposit(
            body.uuid,
            body.currency,
            body.sourceAddress,
            body.transactionRef,
            body.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.startTime), ZoneId.systemDefault())
            },
            body.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.endTime), ZoneId.systemDefault())
            },
            offset,
            size,
            body.ascendingByTime,
        )
    }

}