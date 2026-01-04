package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.ReservedTransferResponse
import co.nilin.opex.api.core.inout.TransferReserveRequest
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/swap")
class SwapController(
    val walletProxy: WalletProxy,
) {
    @PostMapping("/reserve")
    suspend fun reserve(
        @RequestBody request: TransferReserveRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReservedTransferResponse {
        return walletProxy.reserveSwap(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/finalize/{reserveUuid}")
    suspend fun finalizeTransfer(
        @PathVariable reserveUuid: String,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {
        return walletProxy.finalizeSwap(
            securityContext.jwtAuthentication().tokenValue(),
            reserveUuid,
            description,
            transferRef
        )
    }
}