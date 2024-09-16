package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.service.WithdrawService
import co.nilin.opex.wallet.core.spi.WalletDataManager
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin")
class AdminController(private val withdrawService: WithdrawService, private val walletDataManager: WalletDataManager, private val transferService: TransferService) {

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
            @RequestParam("status", required = false) status: List<String>?,
            @RequestParam offset: Int,
            @RequestParam size: Int,
            @RequestParam("ascending_by_time", required = false) ascendingByTime: Boolean?
    ): PagingWithdrawResponse {
        return withdrawService
                .findByCriteria(
                        uuid,
                        withdrawId,
                        currency,
                        destTxRef,
                        destAddress,
                        status?.isEmpty() ?: true,
                        status ?: listOf(""),
                        offset,
                        size,
                        ascendingByTime?:true
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
            @RequestParam("destNote", required = false) destNote: String?,
            @CurrentSecurityContext securityContext: SecurityContext?

    ): WithdrawResult {
        return withdrawService.rejectWithdraw(WithdrawRejectCommand(withdrawId, statusReason, destNote, securityContext?.authentication?.name))
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
            @CurrentSecurityContext securityContext: SecurityContext?

    ): WithdrawResult {

        return withdrawService.acceptWithdraw(WithdrawAcceptCommand(withdrawId, destTransactionRef, destNote, fee, securityContext?.authentication?.name))
    }

    @PostMapping("/withdraw/manually/{amount}_{symbol}/{sourceUuid}")
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
                symbol, securityContext.authentication.name, sourceUuid,
                amount, request
        )
    }

    @PostMapping("/deposit/manually/{amount}_{symbol}/{receiverUuid}")
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
    suspend fun depositManually(
            @PathVariable("symbol") symbol: String,
            @PathVariable("receiverUuid") receiverUuid: String,
            @PathVariable("amount") amount: BigDecimal,
            @RequestBody request: ManualTransferRequest,
            @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return transferService.depositManually(
                symbol, receiverUuid,
                securityContext.authentication.name, amount, request
        )
    }

}