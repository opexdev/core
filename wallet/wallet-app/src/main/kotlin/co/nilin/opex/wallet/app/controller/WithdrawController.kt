package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.RequestWithdrawBody
import co.nilin.opex.wallet.app.dto.SearchWithdrawRequest
import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.app.dto.WithdrawHistoryResponse
import co.nilin.opex.wallet.core.inout.WithdrawCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.WithdrawResult
import co.nilin.opex.wallet.core.service.WithdrawService
import org.springframework.web.bind.annotation.*
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

    @GetMapping
    suspend fun myWithdraws(principal: Principal, @RequestBody body: SearchWithdrawRequest): List<WithdrawResponse> {
        return withdrawService.findByCriteria(
            principal.name,
            body.currency,
            body.destTxRef,
            body.destAddress,
            body.status?.isEmpty() ?: true,
            body.status
        )
    }

    @PostMapping
    suspend fun requestWithdraw(principal: Principal, @RequestBody request: RequestWithdrawBody): WithdrawResult {
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
                    destNote,
                    fee
                )
            }
        )
    }

    @PostMapping("/history/{uuid}")
    suspend fun getWithdrawTransactionsForUser(
        @PathVariable uuid: String,
        @RequestBody request: TransactionRequest
    ): List<WithdrawHistoryResponse> {
        return withdrawService.findWithdrawHistory(
            uuid,
            request.coin,
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
            WithdrawHistoryResponse(
                it.withdrawId,
                it.ownerUuid,
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
                it.createDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                it.acceptDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            )
        }
    }
}