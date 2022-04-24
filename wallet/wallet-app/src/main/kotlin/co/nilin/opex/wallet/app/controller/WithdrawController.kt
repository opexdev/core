package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.app.dto.WithdrawHistoryResponse
import co.nilin.opex.wallet.core.inout.WithdrawCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.WithdrawResult
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
        @RequestParam("currency", required = false) currency: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam("status", required = false) status: List<String>?
    ): List<WithdrawResponse> {
        return withdrawService
            .findByCriteria(
                principal.name,
                withdrawId,
                currency,
                destTxRef,
                destAddress,
                status?.isEmpty() ?: true,
                status ?: listOf(""),
                null,
                null
            )
    }

    @PostMapping("/{amount}_{symbol}")
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
        @PathVariable("symbol") symbol: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("transferRef", required = false) transferRef: String?,
        @RequestParam("fee") fee: BigDecimal,
        @RequestParam("destCurrency") destCurrency: String,
        @RequestParam("destAddress") destAddress: String,
        @RequestParam("destNetwork") destNetwork: String,
        @RequestParam("destNote", required = false) destNote: String?,
    ): WithdrawResult {
        return withdrawService.requestWithdraw(
            WithdrawCommand(
                principal.name, symbol,
                amount, description, transferRef, destCurrency, destAddress, destNetwork, destNote, fee
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
            LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault()),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault()),
            request.limit,
            request.offset
        ).map {
            WithdrawHistoryResponse(
                it.withdrawId,
                it.ownerUuid,
                it.amount,
                it.acceptedFee,
                it.appliedFee,
                it.destAmount,
                it.destCurrency,
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
