package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.CurrencyPair
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.inout.CurrencyPrice
import co.nilin.opex.wallet.core.model.otc.*
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/otc")
class CurrencyRatesController(
    private val rateService: co.nilin.opex.wallet.core.service.otc.RateService,

    private val graphService: GraphService
) {


    @PostMapping("/rate")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun createRate(
        @RequestBody request: SetCurrencyExchangeRateRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        request.validate()
        rateService.addRate(Rate(request.sourceSymbol, request.destSymbol, request.rate), request.ignoreIfExist, securityContext.authentication.name)
    }

    @PutMapping("/rate")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun updateRate(
        @RequestBody request: SetCurrencyExchangeRateRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Rates {
        request.validate()
        return rateService.updateRate(Rate(request.sourceSymbol, request.destSymbol, request.rate), securityContext.authentication.name)
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteRate(
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Rates {
        return rateService.deleteRate(Rate(sourceSymbol, destSymbol, BigDecimal.ZERO), securityContext.authentication.name)
    }

    @GetMapping("/rate")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"rates\": [{\"sourceSymbol\": \"BTC\",\n" +
                        "               \"destSymbol\": \"ETH\",\n" +
                        " \"rate\": \"100.0\"}] }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun fetchRates(): Rates {

        return rateService.getRate()
    }

    //TODO: please verify. With this change we don't support {sourceSymbol}/all or all/{destSymbol} any more, isn't it needed in UI?
    @GetMapping("/rate/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"rates\": [{\"sourceSymbol\": \"BTC\",\n" +
                        "               \"destSymbol\": \"ETH\",\n" +
                        " \"rate\": \"100.0\"}] }",
                mediaType = "application/json"
            )
        )
    )
    suspend fun fetchRate(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rate? {
        return rateService.getRate(sourceSymbol, destSymbol)
    }

    @PostMapping("/forbidden-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun addForbiddenPair(
        @RequestBody request: CurrencyPair,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        request.validate()
        rateService.addForbiddenPair(ForbiddenPair(request.sourceSymbol, request.destSymbol), securityContext.authentication.name)
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteForbiddenPair(
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ForbiddenPairs {
        return rateService.deleteForbiddenPair(ForbiddenPair(sourceSymbol, destSymbol), securityContext.authentication.name)
    }

    @GetMapping("/forbidden-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[{\"sourceSymbol\": \"BTC\",\n" +
                        "               \"destSymbol\": \"ETH\" }]",
                mediaType = "application/json"
            )
        )
    )
    suspend fun fetchForbiddenPairs(): ForbiddenPairs {
        return rateService.getForbiddenPairs()
    }

    @PostMapping("/forbidden-swap-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun addForbiddenSwapPair(
        @RequestBody request: CurrencyPair,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        request.validate()
        rateService.addForbiddenSwapPair(ForbiddenSwapPair(request.sourceSymbol, request.destSymbol), securityContext.authentication.name)
    }

    @DeleteMapping("/forbidden-swap-pairs/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteForbiddenSwapPair(
        @PathVariable sourceSymbol: String,
        @PathVariable destSymbol: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ForbiddenSwapPairs {
        return rateService.deleteForbiddenSwapPair(ForbiddenSwapPair(sourceSymbol, destSymbol), securityContext.authentication.name)
    }

    @GetMapping("/forbidden-swap-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[{\"sourceSymbol\": \"BTC\",\n" +
                        "               \"destSymbol\": \"ETH\" }]",
                mediaType = "application/json"
            )
        )
    )
    suspend fun fetchForbiddenSwapPairs(): ForbiddenSwapPairs {
        return rateService.getForbiddenSwapPairs()
    }

    @PostMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun addTransitiveSymbols(
        @RequestBody symbols: Symbols,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        rateService.addTransitiveSymbols(symbols, securityContext.authentication.name)
    }

    @DeleteMapping("/transitive-symbols/{symbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteTransitiveSymbols(
        @PathVariable symbol: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Symbols {
        return rateService.deleteTransitiveSymbols(Symbols(listOf(symbol)), securityContext.authentication.name)
    }

    @DeleteMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteTransitiveSymbols(
        @RequestBody symbols: Symbols,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Symbols {
        return rateService.deleteTransitiveSymbols(symbols, securityContext.authentication.name)
    }

    @GetMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[\"Z\",\"Y\"]",
                mediaType = "application/json"
            )
        )
    )
    suspend fun fetchTransitiveSymbols(): Symbols {
        return rateService.getTransitiveSymbols()
    }

    //TODO why this endpoint is a POST method?
    @RequestMapping("/route", method = [RequestMethod.POST, RequestMethod.GET])
    suspend fun fetchRoutes(
        @RequestParam("sourceSymbol") sourceSymbol: String? = null,
        @RequestParam("destSymbol") destSymbol: String? = null
    ): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(graphService.getCurrencyExchangeRates(sourceSymbol, destSymbol))
    }

    @GetMapping("/currency/price")
    suspend fun getPrice(@RequestParam("unit") unit: String): List<CurrencyPrice>? {
        return graphService.fetchPrice(unit)
    }

}
