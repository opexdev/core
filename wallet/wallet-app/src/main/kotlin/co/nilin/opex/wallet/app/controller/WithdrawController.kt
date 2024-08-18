package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.app.dto.WithdrawHistoryResponse
import co.nilin.opex.wallet.core.inout.WithdrawCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.WithdrawResult
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
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
    suspend fun findWithdraw(@PathVariable withdrawId: String): WithdrawResponse {
        return with(withdrawService.findByCriteria(withdrawId = withdrawId)) {
            if (isEmpty()) throw OpexError.WithdrawNotFound.exception()
            get(0)
        }
    }

    @GetMapping
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
    suspend fun getMyWithdraws(
        principal: Principal,
        @RequestParam("withdraw_id", required = false) withdrawId: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam(required = false) status: List<WithdrawStatus>?
    ): List<WithdrawResponse> {
        return withdrawService
            .findByCriteria(
                principal.name,
                withdrawId,
                currency,
                destTxRef,
                destAddress,
                status?.isEmpty() ?: true,
                status
            )
    }

    @PostMapping("/{amount}_{currency}")
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
    suspend fun requestWithdraw(
        principal: Principal,
        @PathVariable currency: String,
        @PathVariable amount: BigDecimal,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) transferRef: String?,
        @RequestParam fee: BigDecimal,
        @RequestParam destSymbol: String,
        @RequestParam destAddress: String,
        @RequestParam destNetwork: String?,
        @RequestParam(required = false) destNote: String?,
    ): WithdrawResult {
        return withdrawService.requestWithdraw(
            WithdrawCommand(
                principal.name,
                currency,
                amount,
                description,
                transferRef,
                destSymbol,
                destAddress,
                destNetwork,
                destNote,
                fee
            )
        )
    }

    @PostMapping("/history/{uuid}")
    suspend fun getWithdrawTransactionsForUser(
        @PathVariable("uuid") uuid: String,
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
                it.acceptedFee,
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



