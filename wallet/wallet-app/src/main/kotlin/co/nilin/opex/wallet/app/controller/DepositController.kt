package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.DepositHistoryRequest
import co.nilin.opex.wallet.app.dto.DepositResponse
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.spi.DepositPersister
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
@RequestMapping("/v1/deposit")
class DepositController(
    private val depositPersister: DepositPersister,
    private val transferService: TransferService
) {

    @PostMapping("/history")
    suspend fun getDepositTransactionsForUser(
        @RequestBody request: DepositHistoryRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<DepositResponse> {
        return depositPersister.findDepositHistory(
            securityContext.authentication.name,
            request.currency,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
            request.limit,
            request.offset,
            request.ascendingByTime
        ).map {
            DepositResponse(
                it.id!!,
                it.ownerUuid,
                it.currency,
                it.amount,
                it.network,
                it.note,
                it.transactionRef,
                it.status,
                it.depositType,
                it.createDate
            )
        }
    }


    @PostMapping("/manually/{amount}_{symbol}/{receiverUuid}")
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
        return transferService.depositManually(
            symbol,
            receiverUuid,
            securityContext.authentication.name,
            amount,
            request
        )
    }
}



