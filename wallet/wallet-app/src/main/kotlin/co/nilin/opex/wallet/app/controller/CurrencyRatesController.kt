package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyExchangeRate
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.CurrencyPair
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.app.service.otc.CurrencyGraph
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class CurrencyRatesController {

    @Autowired
    lateinit var currencyGraph: CurrencyGraph

    @GetMapping("/rates/{sourceSymbol}/{destSymbol}")
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
    fun fetchRates(sourceSymbol: String, destSymbol: String): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(
            currencyGraph.getRates().map { rate ->
                CurrencyExchangeRate(
                    rate.sourceSymbol, rate.destSymbol, rate.rate
                )
            }
        )
    }

    @GetMapping("/routes")
    fun fetchRoutes(): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(
            currencyGraph
                .getAvailableRoutes()
                .map { route ->
                    CurrencyExchangeRate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate())
                }
        )
    }

    @PostMapping("/rates")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun updateOrCreateRate(@RequestBody request: SetCurrencyExchangeRateRequest) {
        currencyGraph.addCurrencyRate(request.sourceSymbol, request.destSymbol, request.rate)
    }

    @DeleteMapping("/rates")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun deleteRate(@RequestBody request: CurrencyPair) {
        currencyGraph.removeCurrencyRate(request.sourceSymbol, request.destSymbol)
    }

    @PostMapping("/forbidden-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun addForbiddenPair(@RequestBody request: CurrencyPair) {
        currencyGraph.addForbiddenRateNames(request.sourceSymbol, request.destSymbol)
    }

    @DeleteMapping("/forbidden-pairs")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun deleteForbiddenPair(@RequestBody request: CurrencyPair) {
        currencyGraph.removeForbiddenRateNames(request.sourceSymbol, request.destSymbol)
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
    fun fetchForbiddenPairs(): List<CurrencyPair> {
        return currencyGraph.getForbiddenNames().map { p -> CurrencyPair(p.first, p.second) }
    }

    @PostMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun addTransitiveSymbols(@RequestBody symbols: List<String>) {
        currencyGraph.addTransitiveSymbols(symbols)
    }

    @DeleteMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun deleteTransitiveSymbols(@RequestBody symbols: List<String>) {
        currencyGraph.removeTransitiveSymbols(symbols)
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
    fun fetchTransitiveSymbols(): List<String> {
        return currencyGraph.getTransitiveSymbols()
    }
}