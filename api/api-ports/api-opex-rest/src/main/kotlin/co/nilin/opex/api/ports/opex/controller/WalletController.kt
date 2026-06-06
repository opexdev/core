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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController("walletOpexController")
@RequestMapping("/opex/v1/wallet")
@Tag(name = "Wallet", description = "Authenticated user wallet asset, limits, and deposit address operations.")
class WalletController(
    private val walletProxy: WalletProxy,
    private val bcGatewayProxy: BlockchainGatewayProxy
) {

    @GetMapping("/asset")
    @Operation(
        summary = "Get user assets",
        description = """GET /opex/v1/wallet/asset.
Behavior: If `symbol` is omitted, all user wallet assets are returned. If provided, only that currency asset is returned.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- walletType values in related wallet responses/requests: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = AssetResponse::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun getUserAssets(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "symbol", description = "Trading pair or currency symbol, depending on endpoint.", required = false)
        @RequestParam(required = false) symbol: String?
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
    @Operation(
        summary = "Get wallet owner limits",
        description = """GET /opex/v1/wallet/limits.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- walletType values in related wallet responses/requests: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = OwnerLimitsResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun getWalletOwnerLimits(@Parameter(hidden = true)
                                     @CurrentSecurityContext securityContext: SecurityContext): OwnerLimitsResponse {
        return walletProxy.getOwnerLimits(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue()
        )
    }

    @GetMapping("/deposit/address")
    @Operation(
        summary = "Assign address",
        description = """GET /opex/v1/wallet/deposit/address.
Source of values: `gatewayUuid` should come from server-provided gateway data for the selected currency/network.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- walletType values in related wallet responses/requests: MAIN, EXCHANGE, CASHOUT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = AssignAddressResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun assignAddress(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = true)
        @RequestParam currency: String,
        @Parameter(name = "gatewayUuid", description = "Gateway UUID.", required = true)
        @RequestParam gatewayUuid: String,
        @Parameter(hidden = true)
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
