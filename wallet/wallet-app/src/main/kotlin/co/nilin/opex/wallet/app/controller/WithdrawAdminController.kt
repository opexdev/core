package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.AdminSearchWithdrawRequest
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.service.WithdrawService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin/withdraw")
class WithdrawAdminController(
    private val withdrawService: WithdrawService,
    private val transferService: TransferService
) {

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
        @RequestParam(required = false) destAmount: BigDecimal?,
        @CurrentSecurityContext securityContext: SecurityContext
    ): WithdrawActionResult {
        return withdrawService.acceptWithdraw(
            WithdrawAcceptCommand(
                withdrawId,
                destAmount,
                destTransactionRef,
                destNote,
                securityContext.authentication.name
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
        @RequestParam reason: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): WithdrawActionResult {
        return withdrawService.rejectWithdraw(
            WithdrawRejectCommand(
                withdrawId,
                reason,
                securityContext.authentication.name
            )
        )
    }

    @PostMapping("/manually/{amount}_{symbol}/{sourceUuid}")
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
    suspend fun withdrawManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("sourceUuid") sourceUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return transferService.withdrawManually(
            symbol,
            securityContext.authentication.name,
            sourceUuid,
            amount, request
        )
    }
}