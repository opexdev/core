package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.otc.CurrencyExchangeRatesResponse
import co.nilin.opex.api.core.inout.otc.CurrencyPair
import co.nilin.opex.api.core.inout.otc.CurrencyPrice
import co.nilin.opex.api.core.inout.otc.ForbiddenPairs
import co.nilin.opex.api.core.inout.otc.Rate
import co.nilin.opex.api.core.inout.otc.Rates
import co.nilin.opex.api.core.inout.otc.SetCurrencyExchangeRateRequest
import co.nilin.opex.api.core.inout.otc.Symbols
import co.nilin.opex.api.core.spi.RateProxy
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/otc")
class CurrencyRatesController(
    private val rateProxy: RateProxy
) {

    // Rates
    @PostMapping("/rate")
    @Operation(
        tags = ["OTC Rates"],
        summary = "Create OTC exchange rate",
        description = """Creates a new OTC exchange rate.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Validation:
- `rate` must be greater than zero.
- `sourceSymbol` and `destSymbol` must be different.

Request body: SetCurrencyExchangeRateRequest.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "OTC exchange rate creation payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SetCurrencyExchangeRateRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange rate created successfully. No response body.",
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
    suspend fun createRate(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: SetCurrencyExchangeRateRequest
    ) {
        request.validate()
        rateProxy.createRate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PutMapping("/rate")
    @Operation(
        tags = ["OTC Rates"],
        summary = "Update OTC exchange rate",
        description = """Updates an existing OTC exchange rate.

Required authentication:
- Bearer admin-token or service token is required.
- Required role: ROLE_admin or ROLE_rate_bot.

Validation:
- `rate` must be greater than zero.
- `sourceSymbol` and `destSymbol` must be different.

Request body: SetCurrencyExchangeRateRequest.

Response body: Rates.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "OTC exchange rate update payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SetCurrencyExchangeRateRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange rate updated successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Rates::class)
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
                description = "Forbidden. Required role is missing: ROLE_admin or ROLE_rate_bot. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun updateRate(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: SetCurrencyExchangeRateRequest
    ): Rates {
        request.validate()
        return rateProxy.updateRate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    @Operation(
        tags = ["OTC Rates"],
        summary = "Delete OTC exchange rate",
        description = """Deletes an OTC exchange rate by source and destination symbols.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Path parameters:

Response body: Rates.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "sourceSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Source currency symbol.",
                example = "BTC",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "destSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Destination currency symbol.",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange rate deleted successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Rates::class)
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
    suspend fun deleteRate(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): Rates {
        return rateProxy.deleteRate(securityContext.jwtAuthentication().tokenValue(), sourceSymbol, destSymbol)
    }

    @GetMapping("/rate")
    @Operation(
        tags = ["OTC Rates"],
        summary = "List OTC exchange rates",
        description = """Returns configured OTC exchange rates.

Authentication:
- Public endpoint. No Bearer token is required.

Response body: Rates.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange rates returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Rates::class)
                    )
                ]
            )
        ]
    )
    suspend fun fetchRates(): Rates {
        return rateProxy.fetchRates()
    }

    @GetMapping("/rate/{sourceSymbol}/{destSymbol}")
    @Operation(
        tags = ["OTC Rates"],
        summary = "Get OTC exchange rate",
        description = """Returns one OTC exchange rate by source and destination symbols.

Authentication:
- Public endpoint. No Bearer token is required.

Path parameters:

Response body: Rate, nullable.""",
        parameters = [
            Parameter(
                name = "sourceSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Source currency symbol.",
                example = "BTC",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "destSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Destination currency symbol.",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange rate returned successfully. Response may be null if no rate exists.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Rate::class)
                    )
                ]
            )
        ]
    )
    suspend fun fetchRate(
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): Rate? {
        return rateProxy.fetchRate(sourceSymbol, destSymbol)
    }

    // Forbidden pairs
    @PostMapping("/forbidden-pairs")
    @Operation(
        tags = ["OTC Forbidden Pairs"],
        summary = "Add forbidden OTC pair",
        description = """Adds a forbidden OTC currency pair.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Validation:
- `sourceSymbol` and `destSymbol` must be different.

Request body: CurrencyPair.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Forbidden OTC pair payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CurrencyPair::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Forbidden pair added successfully. No response body.",
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
    suspend fun addForbiddenPair(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: CurrencyPair
    ) {
        request.validate()
        rateProxy.addForbiddenPair(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    @Operation(
        tags = ["OTC Forbidden Pairs"],
        summary = "Delete forbidden OTC pair",
        description = """Deletes one forbidden OTC currency pair.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Path parameters:

Response body: ForbiddenPairs.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "sourceSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Source currency symbol.",
                example = "BTC",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "destSymbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Destination currency symbol.",
                example = "IRR",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Forbidden pair deleted successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ForbiddenPairs::class)
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
    suspend fun deleteForbiddenPair(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): ForbiddenPairs {
        return rateProxy.deleteForbiddenPair(securityContext.jwtAuthentication().tokenValue(), sourceSymbol, destSymbol)
    }

    @GetMapping("/forbidden-pairs")
    @Operation(
        tags = ["OTC Forbidden Pairs"],
        summary = "List forbidden OTC pairs",
        description = """Returns forbidden OTC currency pairs.

Authentication:
- Public endpoint. No Bearer token is required.

Response body: ForbiddenPairs.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Forbidden OTC pairs returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ForbiddenPairs::class)
                    )
                ]
            )
        ]
    )
    suspend fun fetchForbiddenPairs(): ForbiddenPairs {
        return rateProxy.fetchForbiddenPairs()
    }

    // Transitive symbols
    @PostMapping("/transitive-symbols")
    @Operation(
        tags = ["OTC Transitive Symbols"],
        summary = "Add transitive OTC symbols",
        description = """Adds transitive symbols used for OTC route calculation.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Request body: Symbols.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Transitive symbols payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Symbols::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transitive symbols added successfully. No response body.",
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
    suspend fun addTransitiveSymbols(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody symbols: Symbols
    ) {
        rateProxy.addTransitiveSymbols(securityContext.jwtAuthentication().tokenValue(), symbols)
    }

    @DeleteMapping("/transitive-symbols/{symbol}")
    @Operation(
        tags = ["OTC Transitive Symbols"],
        summary = "Delete one transitive OTC symbol",
        description = """Deletes one transitive symbol used for OTC route calculation.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Path parameters:

Response body: Symbols.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "symbol",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Transitive symbol to delete.",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transitive symbol deleted successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Symbols::class)
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
    suspend fun deleteTransitiveSymbols(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable symbol: String
    ): Symbols {
        return rateProxy.deleteTransitiveSymbol(securityContext.jwtAuthentication().tokenValue(), symbol)
    }

    @DeleteMapping("/transitive-symbols")
    @Operation(
        tags = ["OTC Transitive Symbols"],
        summary = "Delete multiple transitive OTC symbols",
        description = """Deletes multiple transitive symbols used for OTC route calculation.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Request body: Symbols.

Response body: Symbols.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Transitive symbols delete payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Symbols::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transitive symbols deleted successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Symbols::class)
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
    suspend fun deleteTransitiveSymbols(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody symbols: Symbols
    ): Symbols {
        return rateProxy.deleteTransitiveSymbols(securityContext.jwtAuthentication().tokenValue(), symbols)
    }

    @GetMapping("/transitive-symbols")
    @Operation(
        tags = ["OTC Transitive Symbols"],
        summary = "List transitive OTC symbols",
        description = """Returns transitive symbols used for OTC route calculation.

Authentication:
- Public endpoint. No Bearer token is required.

Response body: Symbols.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transitive symbols returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Symbols::class)
                    )
                ]
            )
        ]
    )
    suspend fun fetchTransitiveSymbols(): Symbols {
        return rateProxy.fetchTransitiveSymbols()
    }

    // Routes and prices
    @GetMapping("/route")
    @Operation(
        tags = ["OTC Routes & Prices"],
        summary = "Calculate OTC exchange routes",
        description = """Returns calculated OTC exchange routes.

Authentication:
- Public endpoint. No Bearer token is required.

Query parameters:

Route calculation behavior:
- If both sourceSymbol and destSymbol are provided, routes are calculated for that specific pair.
- If sourceSymbol is omitted, routes are calculated from all possible source symbols.
- If destSymbol is omitted, routes are calculated to all possible destination symbols.
- If both are omitted, routes are calculated for all possible symbol combinations.
- Do not send the literal string "null". Omit the query parameter when it should be treated as null.

Response body: CurrencyExchangeRatesResponse.""",
        parameters = [
            Parameter(
                name = "sourceSymbol",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "Optional source currency symbol. If omitted, all possible source symbols are considered. Do not send the literal string \"null\".",
                example = "BTC",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "destSymbol",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "Optional destination currency symbol. If omitted, all possible destination symbols are considered. Do not send the literal string \"null\".",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange routes returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CurrencyExchangeRatesResponse::class)
                    )
                ]
            )
        ]
    )
    suspend fun fetchRoutes(
        @RequestParam("sourceSymbol", required = false) sourceSymbol: String? = null,
        @RequestParam("destSymbol", required = false) destSymbol: String? = null
    ): CurrencyExchangeRatesResponse {
        return rateProxy.fetchRoutes(sourceSymbol, destSymbol)
    }

    @PostMapping("/route")
    @Operation(
        tags = ["OTC Routes & Prices"],
        summary = "Calculate OTC exchange routes as admin",
        description = """Returns calculated OTC exchange routes.

Required authentication:
- Bearer admin-token is required.
- Required role: ROLE_admin.

Query parameters:

Route calculation behavior:
- If both sourceSymbol and destSymbol are provided, routes are calculated for that specific pair.
- If sourceSymbol is omitted, routes are calculated from all possible source symbols.
- If destSymbol is omitted, routes are calculated to all possible destination symbols.
- If both are omitted, routes are calculated for all possible symbol combinations.
- Do not send the literal string "null". Omit the query parameter when it should be treated as null.

Response body: CurrencyExchangeRatesResponse.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "sourceSymbol",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "Optional source currency symbol. If omitted, all possible source symbols are considered. Do not send the literal string \"null\".",
                example = "BTC",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "destSymbol",
                `in` = ParameterIn.QUERY,
                required = false,
                description = "Optional destination currency symbol. If omitted, all possible destination symbols are considered. Do not send the literal string \"null\".",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OTC exchange routes returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CurrencyExchangeRatesResponse::class)
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
    suspend fun fetchRoutesAsAdmin(
        @RequestParam("sourceSymbol", required = false) sourceSymbol: String? = null,
        @RequestParam("destSymbol", required = false) destSymbol: String? = null
    ): CurrencyExchangeRatesResponse {
        return rateProxy.fetchRoutes(sourceSymbol, destSymbol)
    }

    @GetMapping("/currency/price")
    @Operation(
        tags = ["OTC Routes & Prices"],
        summary = "Get OTC currency prices",
        description = """Returns OTC currency prices for the requested unit.

Authentication:
- Public endpoint. No Bearer token is required.

Query parameters:

Response body: Array<CurrencyPrice>.""",
        parameters = [
            Parameter(
                name = "unit",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "Pricing unit.",
                example = "USDT",
                schema = Schema(type = "string")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Currency prices returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = CurrencyPrice::class))
                    )
                ]
            )
        ]
    )
    suspend fun getPrice(
        @RequestParam("unit") unit: String
    ): List<CurrencyPrice> {
        return rateProxy.getPrice(unit)
    }
}
