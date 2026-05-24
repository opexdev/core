package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.CurrencyGatewayUpdateCommand
import co.nilin.opex.api.core.inout.OffChainGatewayCommand
import co.nilin.opex.api.core.inout.OnChainGatewayCommand
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin")
@Tag(name = "Gateway Admin", description = "Admin gateway management for currencies.")
class GatewayAdminController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping("/{currencySymbol}/gateway")
    @Operation(
        summary = "Add currency gateway",
        description = """
Security:
- Bearer admin-token is required.
- Required authority: ROLE_admin.

Behavior:
- OffChain gateways use type=OffChain and transferMethod.
- OnChain native gateways use type=OnChain and isToken=false.
- Token gateways use type=OnChain and isToken=true with token fields.

Source of values:
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- chain values should come from the server-provided chain list.

Response body: CurrencyGatewayCommand.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = SwaggerRequestBody(
            required = true,
            description = "CurrencyGatewayCommand. Use one of: OffChainGatewayCommand, OnChainGatewayCommand, or OnChainGatewayCommand with isToken=true for token gateways.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(
                        oneOf = [OffChainGatewayCommand::class, OnChainGatewayCommand::class],
                        discriminatorProperty = "type"
                    ),
                    examples = [
                        ExampleObject(
                            name = "OffChain gateway",
                            summary = "OffChainGatewayCommand",
                            value = """
{
  "type": "OffChain",
  "currencySymbol": "IRT",
  "gatewayUuid": "ofg-card-irt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 100000,
  "depositMax": 500000000,
  "withdrawMin": 100000,
  "withdrawMax": 500000000,
  "depositDescription": "Card deposit",
  "withdrawDescription": "Card withdraw",
  "displayOrder": 1,
  "transferMethod": "CARD"
}
                            """
                        ),
                        ExampleObject(
                            name = "OnChain native gateway",
                            summary = "OnChainGatewayCommand with isToken=false",
                            value = """
{
  "type": "OnChain",
  "currencySymbol": "BTC",
  "gatewayUuid": "ong-bitcoin-btc",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0.0005,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 0.0001,
  "depositMax": 10,
  "withdrawMin": 0.0001,
  "withdrawMax": 5,
  "depositDescription": "Bitcoin deposit",
  "withdrawDescription": "Bitcoin withdraw",
  "displayOrder": 1,
  "implementationSymbol": "BTC",
  "tokenName": null,
  "tokenAddress": null,
  "isToken": false,
  "decimal": 8,
  "chain": "bitcoin"
}
                            """
                        ),
                        ExampleObject(
                            name = "OnChain token gateway",
                            summary = "OnChainGatewayCommand with isToken=true",
                            value = """
{
  "type": "OnChain",
  "currencySymbol": "USDT",
  "gatewayUuid": "ong-tron-usdt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 1,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 1,
  "depositMax": 100000,
  "withdrawMin": 1,
  "withdrawMax": 50000,
  "depositDescription": "USDT TRC20 deposit",
  "withdrawDescription": "USDT TRC20 withdraw",
  "displayOrder": 2,
  "implementationSymbol": "USDT",
  "tokenName": "Tether USD",
  "tokenAddress": "TX0000000000000000000000000000000000",
  "isToken": true,
  "decimal": 6,
  "chain": "tron"
}
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Gateway created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(
                            oneOf = [OffChainGatewayCommand::class, OnChainGatewayCommand::class],
                            discriminatorProperty = "type"
                        ),
                        examples = [
                            ExampleObject(name = "OffChain gateway response", value = """
{
  "type": "OffChain",
  "currencySymbol": "IRT",
  "gatewayUuid": "ofg-card-irt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 100000,
  "depositMax": 500000000,
  "withdrawMin": 100000,
  "withdrawMax": 500000000,
  "depositDescription": "Card deposit",
  "withdrawDescription": "Card withdraw",
  "displayOrder": 1,
  "transferMethod": "CARD"
}
                            """),
                            ExampleObject(name = "OnChain native gateway response", value = """
{
  "type": "OnChain",
  "currencySymbol": "BTC",
  "gatewayUuid": "ong-bitcoin-btc",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0.0005,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 0.0001,
  "depositMax": 10,
  "withdrawMin": 0.0001,
  "withdrawMax": 5,
  "depositDescription": "Bitcoin deposit",
  "withdrawDescription": "Bitcoin withdraw",
  "displayOrder": 1,
  "implementationSymbol": "BTC",
  "tokenName": null,
  "tokenAddress": null,
  "isToken": false,
  "decimal": 8,
  "chain": "bitcoin"
}
                            """),
                            ExampleObject(name = "OnChain token gateway response", value = """
{
  "type": "OnChain",
  "currencySymbol": "USDT",
  "gatewayUuid": "ong-tron-usdt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 1,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 1,
  "depositMax": 100000,
  "withdrawMin": 1,
  "withdrawMax": 50000,
  "depositDescription": "USDT TRC20 deposit",
  "withdrawDescription": "USDT TRC20 withdraw",
  "displayOrder": 2,
  "implementationSymbol": "USDT",
  "tokenName": "Tether USD",
  "tokenAddress": "TX0000000000000000000000000000000000",
  "isToken": true,
  "decimal": 6,
  "chain": "tron"
}
                            """)
                        ]
                    )
                ]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun addGatewayToCurrency(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody body: CurrencyGatewayCommand,
    ): CurrencyGatewayCommand? {
        return walletProxy.addGatewayToCurrency(securityContext.jwtAuthentication().tokenValue(), currencySymbol, body)
    }

    @PutMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Update currency gateway",
        description = """
Security:
- Bearer admin-token is required.
- Required authority: ROLE_admin.

Behavior:
- OffChain gateways use type=OffChain and transferMethod.
- OnChain native gateways use type=OnChain and isToken=false.
- Token gateways use type=OnChain and isToken=true with token fields.

Source of values:
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.
- chain values should come from the server-provided chain list.

Response body: CurrencyGatewayCommand.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = SwaggerRequestBody(
            required = true,
            description = "CurrencyGatewayCommand. Use one of: OffChainGatewayCommand, OnChainGatewayCommand, or OnChainGatewayCommand with isToken=true for token gateways.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(oneOf = [OffChainGatewayCommand::class, OnChainGatewayCommand::class], discriminatorProperty = "type"),
                    examples = [
                        ExampleObject(name = "OffChain gateway", summary = "OffChainGatewayCommand", value = """
{
  "type": "OffChain",
  "currencySymbol": "IRT",
  "gatewayUuid": "ofg-card-irt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 100000,
  "depositMax": 500000000,
  "withdrawMin": 100000,
  "withdrawMax": 500000000,
  "depositDescription": "Card deposit",
  "withdrawDescription": "Card withdraw",
  "displayOrder": 1,
  "transferMethod": "CARD"
}
                        """),
                        ExampleObject(name = "OnChain native gateway", summary = "OnChainGatewayCommand with isToken=false", value = """
{
  "type": "OnChain",
  "currencySymbol": "BTC",
  "gatewayUuid": "ong-bitcoin-btc",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0.0005,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 0.0001,
  "depositMax": 10,
  "withdrawMin": 0.0001,
  "withdrawMax": 5,
  "depositDescription": "Bitcoin deposit",
  "withdrawDescription": "Bitcoin withdraw",
  "displayOrder": 1,
  "implementationSymbol": "BTC",
  "tokenName": null,
  "tokenAddress": null,
  "isToken": false,
  "decimal": 8,
  "chain": "bitcoin"
}
                        """),
                        ExampleObject(name = "OnChain token gateway", summary = "OnChainGatewayCommand with isToken=true", value = """
{
  "type": "OnChain",
  "currencySymbol": "USDT",
  "gatewayUuid": "ong-tron-usdt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 1,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 1,
  "depositMax": 100000,
  "withdrawMin": 1,
  "withdrawMax": 50000,
  "depositDescription": "USDT TRC20 deposit",
  "withdrawDescription": "USDT TRC20 withdraw",
  "displayOrder": 2,
  "implementationSymbol": "USDT",
  "tokenName": "Tether USD",
  "tokenAddress": "TX0000000000000000000000000000000000",
  "isToken": true,
  "decimal": 6,
  "chain": "tron"
}
                        """)
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Gateway updated successfully.",
                content = [Content(mediaType = "application/json", schema = Schema(oneOf = [OffChainGatewayCommand::class, OnChainGatewayCommand::class], discriminatorProperty = "type"))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun updateGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
        @RequestBody body: CurrencyGatewayUpdateCommand,
    ): CurrencyGatewayCommand? {
        return walletProxy.updateGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            currencySymbol,
            body
        )
    }

    @GetMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Get currency gateway",
        description = """
Security:
- Bearer admin-token is required.
- Required authority: ROLE_admin.

Behavior:
- Response can be OffChainGatewayCommand, OnChainGatewayCommand, or OnChainGatewayCommand with isToken=true for token gateways.

Response body: CurrencyGatewayCommand.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Gateway returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(oneOf = [OffChainGatewayCommand::class, OnChainGatewayCommand::class], discriminatorProperty = "type"),
                        examples = [
                            ExampleObject(name = "OffChain gateway response", value = """
{
  "type": "OffChain",
  "currencySymbol": "IRT",
  "gatewayUuid": "ofg-card-irt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 100000,
  "depositMax": 500000000,
  "withdrawMin": 100000,
  "withdrawMax": 500000000,
  "depositDescription": "Card deposit",
  "withdrawDescription": "Card withdraw",
  "displayOrder": 1,
  "transferMethod": "CARD"
}
                            """),
                            ExampleObject(name = "OnChain native gateway response", value = """
{
  "type": "OnChain",
  "currencySymbol": "BTC",
  "gatewayUuid": "ong-bitcoin-btc",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 0.0005,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 0.0001,
  "depositMax": 10,
  "withdrawMin": 0.0001,
  "withdrawMax": 5,
  "depositDescription": "Bitcoin deposit",
  "withdrawDescription": "Bitcoin withdraw",
  "displayOrder": 1,
  "implementationSymbol": "BTC",
  "tokenName": null,
  "tokenAddress": null,
  "isToken": false,
  "decimal": 8,
  "chain": "bitcoin"
}
                            """),
                            ExampleObject(name = "OnChain token gateway response", value = """
{
  "type": "OnChain",
  "currencySymbol": "USDT",
  "gatewayUuid": "ong-tron-usdt",
  "isDepositActive": true,
  "isWithdrawActive": true,
  "withdrawFee": 1,
  "withdrawAllowed": true,
  "depositAllowed": true,
  "depositMin": 1,
  "depositMax": 100000,
  "withdrawMin": 1,
  "withdrawMax": 50000,
  "depositDescription": "USDT TRC20 deposit",
  "withdrawDescription": "USDT TRC20 withdraw",
  "displayOrder": 2,
  "implementationSymbol": "USDT",
  "tokenName": "Tether USD",
  "tokenAddress": "TX0000000000000000000000000000000000",
  "isToken": true,
  "decimal": 6,
  "chain": "tron"
}
                            """)
                        ]
                    )
                ]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun getGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ): CurrencyGatewayCommand? {
        return walletProxy.getGateway(
            securityContext.jwtAuthentication().tokenValue(),
            gatewayUuid,
            currencySymbol
        )
    }

    @DeleteMapping("/{currencySymbol}/gateway/{uuid}")
    @Operation(
        summary = "Delete currency gateway",
        description = """
Security:
- Bearer admin-token is required.
- Required authority: ROLE_admin.

Response body: No response body.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Gateway deleted successfully. No response body.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. No response body.", content = [Content()])
        ]
    )
    suspend fun deleteGateway(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable("uuid") gatewayUuid: String,
        @PathVariable("currencySymbol") currencySymbol: String,
    ) {
        walletProxy.deleteGateway(securityContext.jwtAuthentication().tokenValue(), gatewayUuid, currencySymbol)
    }
}
