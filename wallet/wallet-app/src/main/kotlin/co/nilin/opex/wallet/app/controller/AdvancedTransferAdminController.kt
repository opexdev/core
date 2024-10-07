package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.dto.ReservedTransferResponse
import co.nilin.opex.wallet.app.dto.TransferPreEvaluateResponse
import co.nilin.opex.wallet.app.dto.TransferReserveRequest
import co.nilin.opex.wallet.app.dto.TransferReserveResponse
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.WalletType
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class AdvancedTransferAdminController {

    @Autowired
    lateinit var transferService: TransferService


    @PostMapping("/admin/v1/swap/history")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{}",
                mediaType = "application/json"
            )
        )
    )
    suspend fun getSwapHistory(
        @PathVariable reserveUuid: String,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @CurrentSecurityContext securityContext: SecurityContext?
    ): TransferResult {
        return transferService.advanceTransfer(
            reserveUuid,
            description,
            transferRef,
            securityContext?.authentication?.name
        )
    }
}