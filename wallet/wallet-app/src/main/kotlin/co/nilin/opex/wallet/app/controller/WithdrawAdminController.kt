package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.AdminSearchWithdrawRequest
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.service.WithdrawService
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin/withdraw/")
class WithdrawAdminController(private val withdrawService: WithdrawService) {

    @GetMapping("/{id}")
    suspend fun getWithdraw(@PathVariable id: Long): WithdrawResponse {
        return withdrawService.findWithdraw(id) ?: throw OpexError.WithdrawNotFound.exception()
    }

    @PostMapping("/search")
    suspend fun search(
        @RequestParam offset: Int,
        @RequestParam size: Int,
        @RequestBody body: AdminSearchWithdrawRequest
    ): List<WithdrawResponse> {
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
    ): WithdrawActionResult {
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
    suspend fun processWithdraw(@PathVariable withdrawId: Long): WithdrawActionResult {
        return withdrawService.processWithdraw(withdrawId)
    }

    @PostMapping("/{withdrawId}/reject")
    suspend fun rejectWithdraw(
        @PathVariable withdrawId: Long,
        @RequestParam reason: String
    ): WithdrawActionResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId, reason))
    }
}