package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.pricemanagement.PairRateConfigView
import co.nilin.opex.api.core.inout.pricemanagement.ProviderPrice
import co.nilin.opex.api.core.inout.pricemanagement.UpsertPairRateConfigRequest
import co.nilin.opex.api.core.spi.PriceManagementProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/price-management/rate-config")
class PriceManagementController(
    private val priceManagementProxy: PriceManagementProxy
) {

    @GetMapping
    @Operation(
        tags = ["Price Management"],
        summary = "List pair rate configs",
        description = """Returns every symbol's rate config, its selected providers, and its priceMode.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Rate configs returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = PairRateConfigView::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required role is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun list(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<PairRateConfigView> {
        return priceManagementProxy.getConfigs(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/{symbol}")
    @Operation(
        tags = ["Price Management"],
        summary = "Get a pair rate config",
        description = """Returns one symbol's rate config, its selected providers, and its priceMode.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Path parameters:""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "symbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Symbol.",
                example = "BTC-USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Rate config returned successfully.",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = PairRateConfigView::class))
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required role is missing: ROLE_admin. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No rate config found for this symbol. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun get(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable symbol: String
    ): PairRateConfigView {
        return priceManagementProxy.getConfig(securityContext.jwtAuthentication().tokenValue(), symbol)
    }

    @PostMapping
    @Operation(
        tags = ["Price Management"],
        summary = "Create or update a pair rate config",
        description = """Creates or fully replaces a symbol's config, its selected providers, and — when
priceMode is MANUAL and a price is included — its price, all in one call.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Request body: UpsertPairRateConfigRequest.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Rate config upsert payload.",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = UpsertPairRateConfigRequest::class))
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Rate config saved successfully.",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = PairRateConfigView::class))
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request, e.g. missing strategy/margin for AUTO mode, or strategy/margin/price set for MANUAL mode. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required role is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun upsert(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpsertPairRateConfigRequest
    ): PairRateConfigView {
        return priceManagementProxy.upsertConfig(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping("/{symbol}/providers")
    @Operation(
        tags = ["Price Management"],
        summary = "Get live provider prices for a symbol",
        description = """Returns the raw, per-provider prices rate-scanner currently reports for a symbol —
useful for picking which providers to include when creating/updating a config.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Path parameters:""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "symbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Symbol.",
                example = "BTC-USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Provider prices returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = ProviderPrice::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required role is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getProvidersPrice(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable symbol: String
    ): List<ProviderPrice> {
        return priceManagementProxy.getProvidersPrice(securityContext.jwtAuthentication().tokenValue(), symbol)
    }
}
