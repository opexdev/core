package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.RequestWithdrawBody
import co.nilin.opex.wallet.app.dto.SearchWithdrawRequest
import co.nilin.opex.wallet.app.dto.WithdrawHistoryRequest
import co.nilin.opex.wallet.core.inout.WithdrawCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.WithdrawActionResult
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/withdraw")
class WithdrawController(private val withdrawService: WithdrawService) {

    @GetMapping("/{withdrawId}")
    suspend fun findWithdraw(@PathVariable withdrawId: Long): WithdrawResponse {
        return withdrawService.findWithdraw(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
    }

    @PostMapping("/search")
    suspend fun myWithdraws(principal: Principal, @RequestBody body: SearchWithdrawRequest): List<WithdrawResponse> {
        return withdrawService.findByCriteria(
            principal.name,
            body.currency,
            body.destTxRef,
            body.destAddress,
            body.status
        )
    }

    @PostMapping
    suspend fun requestWithdraw(principal: Principal, @RequestBody request: RequestWithdrawBody): WithdrawActionResult {
        return withdrawService.requestWithdraw(
            with(request) {
                WithdrawCommand(
                    principal.name,
                    currency,
                    amount,
                    description,
                    destSymbol,
                    destAddress,
                    destNetwork,
                    destNote
                )
            }
        )
    }


    @PostMapping("/{withdrawId}/cancel")
    suspend fun cancelWithdraw(principal: Principal, @PathVariable withdrawId: Long) {
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
        )
    }
}
