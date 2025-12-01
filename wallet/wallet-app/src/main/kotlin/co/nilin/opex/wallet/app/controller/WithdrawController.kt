package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.common.security.tokenValue
import co.nilin.opex.wallet.app.dto.RequestWithdrawBody
import co.nilin.opex.wallet.app.dto.WithdrawHistoryRequest
import co.nilin.opex.wallet.app.utils.asLocalDateTime
import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.inout.WithdrawActionResult
import co.nilin.opex.wallet.core.inout.WithdrawCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.otp.OTPType
import co.nilin.opex.wallet.core.inout.otp.TempOtpResponse
import co.nilin.opex.wallet.core.service.WithdrawService
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/withdraw")
class WithdrawController(private val withdrawService: WithdrawService) {

    @GetMapping("/{withdrawUuid}")
    suspend fun findWithdraw(@PathVariable withdrawUuid: String): WithdrawResponse {
        return withdrawService.findWithdraw(withdrawUuid) ?: throw OpexError.WithdrawNotFound.exception()
    }

    @PostMapping
    suspend fun requestWithdraw(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: RequestWithdrawBody
    ): WithdrawActionResult {
        return withdrawService.requestWithdraw(
            with(request) {
                WithdrawCommand(
                    securityContext.authentication.name,
                    currency,
                    amount,
                    destSymbol,
                    destAddress,
                    destNetwork,
                    destNote,
                    gatewayUuid,
                    null,
                    null
                )
            }, securityContext.jwtAuthentication().tokenValue()
        )
    }

    @PostMapping("/{withdrawId}/otp/{otpType}/request")
    suspend fun requestOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: String,
        @PathVariable otpType: OTPType
    ): TempOtpResponse {
        return withdrawService.requestOTP(
            securityContext.jwtAuthentication().tokenValue(),
            securityContext.authentication.name,
            withdrawId,
            otpType
        )
    }

    @PostMapping("/{withdrawId}/otp/{otpType}/verify")
    suspend fun verifyOTP(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable withdrawId: String,
        @PathVariable otpType: OTPType,
        @RequestParam otpCode: String,
    ): WithdrawActionResult {
        return withdrawService.verifyOTP(securityContext.jwtAuthentication().tokenValue(), withdrawId, otpType, otpCode)
    }

    @PostMapping("/{withdrawId}/cancel")
    suspend fun cancelWithdraw(principal: Principal, @PathVariable withdrawId: String) {
        withdrawService.cancelWithdraw(principal.name, withdrawId)
    }

    @PostMapping("/history")
    suspend fun getWithdrawTransactionsForUser(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: WithdrawHistoryRequest,
    ): List<WithdrawResponse> {
        return withdrawService.findWithdrawHistory(
            securityContext.authentication.name,
            request.currency,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
            request.limit!!,
            request.offset!!,
            request.ascendingByTime
        ).map {
            WithdrawResponse(
                it.withdrawId,
                it.uuid,
                it.amount,
                it.currency,
                it.appliedFee,
                it.destAmount,
                it.destSymbol,
                it.destAddress,
                it.destNetwork,
                it.destNote,
                it.destTransactionRef,
                it.statusReason,
                it.status,
                it.applicator,
                it.withdrawType,
                it.attachment,
                it.createDate,
                it.lastUpdateDate,
                it.transferMethod,
                it.otpRequired
            )
        }
    }

    @PostMapping("/history/count")
    suspend fun getWithdrawTransactionsCountForUser(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: WithdrawHistoryRequest,
    ): Long {
        return withdrawService.findWithdrawHistoryCount(
            securityContext.authentication.name,
            request.currency,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
        )
    }

    @GetMapping("/summary/{uuid}")
    suspend fun getUserWithdrawSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @PathVariable uuid: String,
    ): List<TransactionSummary> {
        return withdrawService.getWithdrawSummary(
            uuid,
            startTime?.asLocalDateTime(),
            endTime?.asLocalDateTime(),
            limit,
        )
    }


}



