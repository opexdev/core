package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AssignAddressRequest
import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.data.AssetResponse
import co.nilin.opex.api.ports.opex.data.AssignAddressResponse
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.common.OpexError
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("walletOpexController")
@RequestMapping("/opex/v1/wallet")
class WalletController(
    private val walletProxy: WalletProxy,
    private val bcGatewayProxy: BlockchainGatewayProxy,
) {

    @GetMapping("/asset")
    suspend fun getUserAssets(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestParam(required = false) symbol: String?,
    ): List<AssetResponse> {
        val auth = securityContext.jwtAuthentication()
        val result = arrayListOf<AssetResponse>()

        if (symbol != null) {
            val wallet = walletProxy.getWallet(auth.name, auth.tokenValue(), symbol.uppercase())
            result.add(AssetResponse(wallet.asset, wallet.balance, wallet.locked, wallet.withdraw))
        } else {
            result.addAll(
                walletProxy.getWallets(auth.name, auth.tokenValue())
                    .map { AssetResponse(it.asset, it.balance, it.locked, it.withdraw) }
            )
        }
        return result
    }

    @GetMapping("/limits")
    suspend fun getWalletOwnerLimits(@CurrentSecurityContext securityContext: SecurityContext): OwnerLimitsResponse {
        return walletProxy.getOwnerLimits(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
        )
    }

    @GetMapping("/deposit/address")
    suspend fun assignAddress(
        @RequestParam currency: String,
        @RequestParam gatewayUuid: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AssignAddressResponse {

        val response = bcGatewayProxy.assignAddress(
            AssignAddressRequest(
                securityContext.authentication.name,
                currency,
                gatewayUuid
            )
        )
        val address = response?.addresses
        if (address.isNullOrEmpty()) throw OpexError.InternalServerError.exception()
        return AssignAddressResponse(address[0].address, currency, address[0].expTime, address[0].assignedDate)
    }
}
