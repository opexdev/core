package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.otc.*
import co.nilin.opex.api.core.spi.RateProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(  "/opex/v1/otc")
class CurrencyRatesController(
    private val rateProxy: RateProxy
) {

    // Rates
    @PostMapping("/rate")
    suspend fun createRate(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: SetCurrencyExchangeRateRequest
    ) {
        request.validate()
        rateProxy.createRate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PutMapping("/rate")
    suspend fun updateRate(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: SetCurrencyExchangeRateRequest
    ): Rates {
        request.validate()
        return rateProxy.updateRate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    suspend fun deleteRate(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): Rates {
        return rateProxy.deleteRate(securityContext.jwtAuthentication().tokenValue(), sourceSymbol, destSymbol)
    }

    @GetMapping("/rate")
    suspend fun fetchRates(): Rates {
        return rateProxy.fetchRates()
    }

    @GetMapping("/rate/{sourceSymbol}/{destSymbol}")
    suspend fun fetchRate(
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): Rate? {
        return rateProxy.fetchRate(sourceSymbol, destSymbol)
    }

    // Forbidden pairs
    @PostMapping("/forbidden-pairs")
    suspend fun addForbiddenPair(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: CurrencyPair
    ) {
        request.validate()
        rateProxy.addForbiddenPair(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    suspend fun deleteForbiddenPair(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String
    ): ForbiddenPairs {
        return rateProxy.deleteForbiddenPair(securityContext.jwtAuthentication().tokenValue(), sourceSymbol, destSymbol)
    }

    @GetMapping("/forbidden-pairs")
    suspend fun fetchForbiddenPairs(): ForbiddenPairs {
        return rateProxy.fetchForbiddenPairs()
    }

    // Transitive symbols
    @PostMapping("/transitive-symbols")
    suspend fun addTransitiveSymbols(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody symbols: Symbols
    ) {
        rateProxy.addTransitiveSymbols(securityContext.jwtAuthentication().tokenValue(), symbols)
    }

    @DeleteMapping("/transitive-symbols/{symbol}")
    suspend fun deleteTransitiveSymbols(
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable symbol: String
    ): Symbols {
        return rateProxy.deleteTransitiveSymbol(securityContext.jwtAuthentication().tokenValue(), symbol)
    }

    @DeleteMapping("/transitive-symbols")
    suspend fun deleteTransitiveSymbols(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody symbols: Symbols
    ): Symbols {
        return rateProxy.deleteTransitiveSymbols(securityContext.jwtAuthentication().tokenValue(), symbols)
    }

    @GetMapping("/transitive-symbols")
    suspend fun fetchTransitiveSymbols(): Symbols {
        return rateProxy.fetchTransitiveSymbols()
    }

    // Routes and prices
    @RequestMapping("/route", method = [RequestMethod.POST, RequestMethod.GET])
    suspend fun fetchRoutes(
        @RequestParam("sourceSymbol") sourceSymbol: String? = null,
        @RequestParam("destSymbol") destSymbol: String? = null
    ): CurrencyExchangeRatesResponse {
        return rateProxy.fetchRoutes(sourceSymbol, destSymbol)
    }

    @GetMapping("/currency/price")
    suspend fun getPrice(
        @RequestParam("unit") unit: String
    ): List<CurrencyPrice> {
        return rateProxy.getPrice(unit)
    }
}
