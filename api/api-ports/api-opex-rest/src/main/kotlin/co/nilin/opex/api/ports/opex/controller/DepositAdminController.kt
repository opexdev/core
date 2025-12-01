package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ManualTransferRequest
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/admin/deposit")
class DepositAdminController(
    private val walletProxy: WalletProxy,
) {
    @PostMapping("/manually/{amount}_{symbol}/{receiverUuid}")
    suspend fun depositManually(
        @PathVariable("symbol") symbol: String,
        @PathVariable("receiverUuid") receiverUuid: String,
        @PathVariable("amount") amount: BigDecimal,
        @RequestBody request: ManualTransferRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.depositManually(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            receiverUuid,
            amount,
            request
        )
    }

}