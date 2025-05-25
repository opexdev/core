package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.inout.TransferResult
import co.nilin.opex.api.core.inout.WalletType
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.data.AssetResponse
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/opex/v1/deposit")
class DepositController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping("/{amount}_{chain}_{symbol}/{receiverUuid}_{receiverWalletType}")
    suspend fun deposit(
        @PathVariable symbol: String,
        @PathVariable receiverUuid: String,
        @PathVariable receiverWalletType: WalletType,
        @PathVariable amount: BigDecimal,
        @RequestParam description: String?,
        @RequestParam transferRef: String?,
        @RequestParam gatewayUuid: String?,
        @PathVariable chain: String?,
    ): TransferResult? {
        return walletProxy.deposit(
            symbol,
            receiverUuid,
            receiverWalletType,
            amount,
            description,
            transferRef,
            gatewayUuid,
            chain
        )
    }
}