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
            body.status,
            offset,
            size
        )
    }

    @PostMapping("/{withdrawId}/accept")
    suspend fun acceptWithdraw(
        @PathVariable withdrawId: Long,
        @RequestParam destTransactionRef: String,
        @RequestParam(required = false) destNote: String?,
        @RequestParam(required = false) destAmount: BigDecimal?
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

    @PostMapping("/{withdrawId}/process")
    suspend fun processWithdraw(@PathVariable withdrawId: Long): WithdrawResult {
        return withdrawService.processWithdraw(withdrawId)
    }

    @PostMapping("/{withdrawId}/reject")
    suspend fun rejectWithdraw(
        @PathVariable withdrawId: Long,
        @RequestParam reason: String
    ): WithdrawResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId, reason))
    }
}