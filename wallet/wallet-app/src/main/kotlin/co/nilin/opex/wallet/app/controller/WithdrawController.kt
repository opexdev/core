package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.inout.*
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
class WithdrawController(private val withdrawService: WithdrawService) {

    data class WithdrawHistoryResponse(
        val withdrawId: Long? = null,
        val uuid: String,
        val amount: BigDecimal,
        val acceptedFee: BigDecimal,
        val appliedFee: BigDecimal?,
        val destAmount: BigDecimal?,
        val destCurrency: String?,
        val destAddress: String?,
        val destNetwork: String?,
        var destNote: String?,
        var destTransactionRef: String?,
        val statusReason: String?,
        val status: String,
        val createDate: Long,
        val acceptDate: Long?
    )

    @GetMapping("/admin/withdraw")
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
    suspend fun searchWithdraws(
        @RequestParam("uuid", required = false) uuid: String?,
        @RequestParam("withdraw_id", required = false) withdrawId: String?,
        @RequestParam("currency", required = false) currency: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam("status", required = false) status: List<String>?
    ): List<WithdrawResponse> {
        return withdrawService
            .findByCriteria(
                uuid,
                withdrawId,
                currency,
                destTxRef,
                destAddress,
                status?.isEmpty() ?: true,
                status ?: listOf("")
            )
    }

    @GetMapping("/withdraw")
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
                status ?: listOf("")
            )
    }

    @PostMapping("/withdraw/{amount}_{symbol}")
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

    @PostMapping(
        "/admin/withdraw/{id}/reject"
    )
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
    suspend fun rejectWithdraw(
        @PathVariable("id") withdrawId: String,
        @RequestParam("statusReason") statusReason: String,
        @RequestParam("destNote", required = false) destNote: String?
    ): WithdrawResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId, statusReason, destNote))
    }

    @PostMapping("/admin/withdraw/{id}/accept")
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
    suspend fun acceptWithdraw(
        @PathVariable("id") withdrawId: String,
        @RequestParam("destTransactionRef", required = false) destTransactionRef: String?,
        @RequestParam("destNote", required = false) destNote: String?,
        @RequestParam("fee", required = false) fee: BigDecimal = BigDecimal.ZERO,
    ): WithdrawResult {
        return withdrawService.acceptWithdraw(WithdrawAcceptCommand(withdrawId, destTransactionRef, destNote, fee))
    }

    @PostMapping("/withdraw/history/{uuid}")
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
