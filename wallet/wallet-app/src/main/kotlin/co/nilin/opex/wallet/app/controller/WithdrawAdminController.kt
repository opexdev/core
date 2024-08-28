package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.AdminSearchWithdrawRequest
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin/withdraw/")
class WithdrawAdminController(private val withdrawService: WithdrawService) {

    @GetMapping("/{id}")
    suspend fun getWithdraw(@PathVariable id: Long): WithdrawResponse {
        return withdrawService.findWithdraw(id) ?: throw OpexError.WithdrawNotFound.exception()
    }

    @GetMapping
    suspend fun search(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchWithdrawRequest
    ): PagingWithdrawResponse {
        return withdrawService.findByCriteria(
            body.uuid,
            body.currency,
            body.destTxRef,
            body.destAddress,
            body.status?.isEmpty() ?: true,
            body.status,
            offset,
            size
        )
    }

    @PostMapping("/{withdrawId}/reject")
    suspend fun rejectWithdraw(
        @PathVariable withdrawId: Long,
        @RequestParam statusReason: String
    ): WithdrawResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId, statusReason))
    }

    @PostMapping("/{withdrawId}/accept")
    suspend fun acceptWithdraw(
        @PathVariable withdrawId: Long,
        @RequestParam destAmount: BigDecimal,
        @RequestParam destTransactionRef: String,
        @RequestParam(required = false) destNote: String?
    ): WithdrawResult {
        return withdrawService.acceptWithdraw(
            WithdrawAcceptCommand(
                withdrawId,
                destAmount,
                destTransactionRef,
                destNote
            )
        )
    }
}