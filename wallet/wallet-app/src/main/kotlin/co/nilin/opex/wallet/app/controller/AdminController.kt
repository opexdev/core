package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.core.inout.WithdrawAcceptCommand
import co.nilin.opex.wallet.core.inout.WithdrawRejectCommand
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.inout.WithdrawResult
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin")
class AdminController(private val withdrawService: WithdrawService) {

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

    @PostMapping("/withdraw/{id}/reject")
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

    @PostMapping("/withdraw/{id}/accept")
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

}