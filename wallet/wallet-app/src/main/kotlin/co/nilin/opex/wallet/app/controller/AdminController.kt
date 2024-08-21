package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin")
@Deprecated("v2 will be used")
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
        @RequestParam(required = false) uuid: String?,
        @RequestParam("withdraw_id", required = false) withdrawId: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam("dest_transaction_ref", required = false) destTxRef: String?,
        @RequestParam("dest_address", required = false) destAddress: String?,
        @RequestParam(required = false) status: List<WithdrawStatus>?,
        @RequestParam offset: Int,
        @RequestParam size: Int
    ): PagingWithdrawResponse {
        return withdrawService.findByCriteria(
            uuid,
            currency,
            destTxRef,
            destAddress,
            status?.isEmpty() ?: true,
            status,
            offset,
            size
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
        @RequestParam statusReason: String,
        @RequestParam(required = false) destNote: String?
    ): WithdrawResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId.toLong(), statusReason, destNote))
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
        @RequestParam(required = false) destTransactionRef: String,
        @RequestParam(required = false) destNote: String?,
        @RequestParam(required = false) fee: BigDecimal = BigDecimal.ZERO,
    ): WithdrawResult {
        return withdrawService.acceptWithdraw(
            WithdrawAcceptCommand(
                withdrawId.toLong(),
                BigDecimal.ZERO,
                destTransactionRef,
                destNote
            )
        )
    }

}