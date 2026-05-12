package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.otc.*
import co.nilin.opex.api.core.spi.RateProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/otc")
@Tag(name = "OTC / Currency Rates", description = "Manage OTC currency exchange rates, forbidden pairs, transitive symbols, routes, and currency prices.")
class CurrencyRatesController(
    private val rateProxy: RateProxy
) {

    // Rates
    @PostMapping("/rate")
    @Operation(
        summary = "Create rate",
        description = """
    POST /opex/v1/otc/rate.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Update rate",
        description = """
    PUT /opex/v1/otc/rate.
    Bearer admin-token required. Required authority: ROLE_admin or ROLE_rate_bot.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Rates::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin or ROLE_rate_bot. No response body.", content = [Content()])
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
        summary = "Delete rate",
        description = """
    DELETE /opex/v1/otc/rate/{sourceSymbol}/{destSymbol}.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Rates::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Fetch rates",
        description = """
    GET /opex/v1/otc/rate.
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Rates::class))])
        ]
    )
    suspend fun fetchRates(): Rates {
        return rateProxy.fetchRates()
    }

    @GetMapping("/rate/{sourceSymbol}/{destSymbol}")
    @Operation(
        summary = "Fetch rate",
        description = """
    GET /opex/v1/otc/rate/{sourceSymbol}/{destSymbol}.
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Rate::class))])
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
        summary = "Add forbidden pair",
        description = """
    POST /opex/v1/otc/forbidden-pairs.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Delete forbidden pair",
        description = """
    DELETE /opex/v1/otc/forbidden-pairs/{sourceSymbol}/{destSymbol}.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ForbiddenPairs::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Fetch forbidden pairs",
        description = """
    GET /opex/v1/otc/forbidden-pairs.
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ForbiddenPairs::class))])
        ]
    )
    suspend fun fetchForbiddenPairs(): ForbiddenPairs {
        return rateProxy.fetchForbiddenPairs()
    }

    // Transitive symbols
    @PostMapping("/transitive-symbols")
    @Operation(
        summary = "Add transitive symbols",
        description = """
    POST /opex/v1/otc/transitive-symbols.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Delete transitive symbols",
        description = """
    DELETE /opex/v1/otc/transitive-symbols/{symbol}.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Symbols::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Delete transitive symbols",
        description = """
    DELETE /opex/v1/otc/transitive-symbols.
    Bearer admin-token required. Required authority: ROLE_admin.
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Symbols::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Fetch transitive symbols",
        description = """
    GET /opex/v1/otc/transitive-symbols.
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = Symbols::class))])
        ]
    )
    suspend fun fetchTransitiveSymbols(): Symbols {
        return rateProxy.fetchTransitiveSymbols()
    }

    // Routes and prices
    @GetMapping("/route")
    @Operation(
        summary = "Fetch routes",
        description = """
    GET /opex/v1/otc/route.
    Optional sourceSymbol and destSymbol behavior: omit sourceSymbol to include all source symbols; omit destSymbol to include all destination symbols; omit both to calculate all possible symbol combinations. Do not send the literal string "null".
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyExchangeRatesResponse::class))])
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
        summary = "Fetch routes as admin",
        description = """
    POST /opex/v1/otc/route.
    Bearer admin-token required. Required authority: ROLE_admin.
    Optional sourceSymbol and destSymbol behavior: omit sourceSymbol to include all source symbols; omit destSymbol to include all destination symbols; omit both to calculate all possible symbol combinations. Do not send the literal string "null".
            """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrencyExchangeRatesResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: ROLE_admin. No response body.", content = [Content()])
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
        summary = "Get price",
        description = """
    GET /opex/v1/otc/currency/price.
            """,
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CurrencyPrice::class)))])
        ]
    )
    suspend fun getPrice(
        @RequestParam("unit") unit: String
    ): List<CurrencyPrice> {
        return rateProxy.getPrice(unit)
    }
}
