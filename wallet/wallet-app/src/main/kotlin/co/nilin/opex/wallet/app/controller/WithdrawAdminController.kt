package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.AdminSearchWithdrawRequest
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.ManualWithdrawService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.service.WithdrawService
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
@RequestMapping("/admin/withdraw")
class WithdrawAdminController(
    private val withdrawService: WithdrawService,
    private val manualWithdrawService: ManualWithdrawService
) {
    data class WithdrawRejectRequest(val reason: String, val attachment: String?)
    data class WithdrawDoneRequest(
        val destTransactionRef: String,
        val destNote: String?,
        val destAmount: BigDecimal?,
        val attachment: String?
    )

    @GetMapping("/{withdrawUuid}")
    suspend fun getWithdraw(@PathVariable withdrawUuid: String): WithdrawResponse {
        return withdrawService.findWithdraw(withdrawUuid) ?: throw OpexError.WithdrawNotFound.exception()
    }

    @Deprecated("endpoint changed")
    @PostMapping("/search")
    suspend fun search(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchWithdrawRequest
    ): List<WithdrawAdminResponse> {
        return withdrawService.findByCriteria(
            body.uuid,
            body.currency,
            body.destTxRef,
            body.destAddress,
            body.status,
            body.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.startTime), ZoneId.systemDefault())
            },
            body.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.endTime), ZoneId.systemDefault())
            },
            body.ascendingByTime,
            offset,
            size
        )
    }

    @PostMapping("/history")
    suspend fun getWithdrawHistory(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchWithdrawRequest
    ): List<WithdrawAdminResponse> {
        return withdrawService.findByCriteria(
            body.uuid,
            body.currency,
            body.destTxRef,
            body.destAddress,
            body.status,
            body.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.startTime), ZoneId.systemDefault())
            },
            body.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(body.endTime), ZoneId.systemDefault())
            },
            body.ascendingByTime,
            offset,
            size
        )
    }

    @PostMapping("/{withdrawUuid}/accept")
    suspend fun acceptWithdraw(
        @PathVariable withdrawUuid: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): WithdrawActionResult {
        return withdrawService.acceptWithdraw(withdrawUuid, securityContext.authentication.name)
    }

    @PostMapping("/{withdrawUuid}/done")
    suspend fun doneWithdraw(
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawDoneRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): WithdrawActionResult {
        return withdrawService.doneWithdraw(
            WithdrawDoneCommand(
                withdrawUuid,
                request.destAmount,
                request.destTransactionRef,
                request.destNote,
                request.attachment,
                securityContext.authentication.name
            )
        )
    }

    @PostMapping("/{withdrawUuid}/reject")
    suspend fun rejectWithdraw(
        @PathVariable withdrawUuid: String,
        @RequestBody request: WithdrawRejectRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): WithdrawActionResult {
        return withdrawService.rejectWithdraw(
            WithdrawRejectCommand(
                withdrawUuid,
                request.reason,
                request.attachment,
                securityContext.authentication.name
            )
        )
    }

    @PostMapping("/manually/{amount}_{symbol}/{sourceUuid}")
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
    suspend fun withdrawManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("sourceUuid") sourceUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return manualWithdrawService.withdrawManually(
            symbol,
            securityContext.authentication.name,
            sourceUuid,
            amount, request
        )
    }
}