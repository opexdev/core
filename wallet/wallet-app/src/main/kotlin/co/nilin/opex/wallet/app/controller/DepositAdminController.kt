package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.*
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/admin")

class DepositAdminController(private val transferService: TransferService) {


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