package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.WalletDataResponse
import co.nilin.opex.api.core.inout.WalletTotal
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/wallet")
class WalletAdminController(
    private val walletProxy: WalletProxy
) {

    @GetMapping("/users")
    suspend fun walletData(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam(required = false) uuid: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam(required = false, defaultValue = "false") excludeSystem: Boolean,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?
    ): List<WalletDataResponse> {
        return walletProxy.getUsersWallets(
            securityContext.jwtAuthentication().tokenValue(),
            uuid,
            currency,
            excludeSystem,
            limit ?: 10,
            offset ?: 0
        )
    }

    @GetMapping("/system/total")
    suspend fun systemWalletTotal(@CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal> {
        return walletProxy.getSystemWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/users/total")
    suspend fun userWalletTotal(@CurrentSecurityContext securityContext: SecurityContext): List<WalletTotal>? {
        return walletProxy.getUsersWalletsTotal(securityContext.jwtAuthentication().tokenValue())
    }
}
