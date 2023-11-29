package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.app.service.otc.CurrencyGraph
import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.otc.ForbiddenPairs
import co.nilin.opex.wallet.core.model.otc.Rate
import co.nilin.opex.wallet.core.model.otc.Rates
import co.nilin.opex.wallet.core.spi.CurrencyService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/otc")
class CurrencyRatesController(private val currencyService: CurrencyService) {

    @Autowired
    lateinit var currencyGraph: CurrencyGraph

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody request: Currency): Currency? {
        return currencyService.addCurrency(request)
    }

    @PutMapping("/currency")
    suspend fun updateCurrency(@RequestBody request: Currency): Currency? {
        return currencyService.updateCurrency(request)
    }

    @GetMapping("/currency/{symbol}")
    suspend fun getCurrency(@PathVariable("symbol") symbol: String): Currency? {
        return currencyService.getCurrency(symbol)
    }

    @GetMapping("/currency")
    suspend fun getCurrencies(): Currencies {
        return currencyService.getCurrencies()
    }

    @DeleteMapping("/currency/{symbol}")
    suspend fun deleteCurrency(@PathVariable("symbol") symbol: String): Currencies {
        return currencyService.deleteCurrency(symbol)
    }


    @PostMapping("/rate")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun createRate(@RequestBody request: SetCurrencyExchangeRateRequest) {
        //  currencyGraph.addCurrencyRate(request.sourceSymbol, request.destSymbol, request.rate)
        currencyGraph.addCurrencyRateV2(request.sourceSymbol, request.destSymbol, request.rate)
    }


    @PutMapping("/rate")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun updateRate(@RequestBody request: SetCurrencyExchangeRateRequest) {
        //  currencyGraph.addCurrencyRate(request.sourceSymbol, request.destSymbol, request.rate)
        currencyGraph.updateRate(Rate(request.sourceSymbol, request.destSymbol, request.rate))
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun deleteRate(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rates {
//        currencyGraph.removeCurrencyRate(sourceSymbol, destSymbol)
        return currencyGraph.removeCurrencyRateV2(sourceSymbol, destSymbol)
    }


    @GetMapping("/routes")
    suspend fun fetchRoutes(): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(
                currencyGraph
                        .getAvailableRoutes()
                        .map { route ->
                            CurrencyExchangeRate(route.getSourceSymbol(), route.getDestSymbol(), route.getRate())
                        }
        )
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
//        return CurrencyExchangeRatesResponse(
//                currencyGraph.getRates()
//                        .filter { rate ->
//                            (sourceSymbol == "all" || rate.sourceSymbol == sourceSymbol)
//                                    && (destSymbol == "all" || rate.destSymbol == destSymbol)
//                        }
//                        .map { rate ->
//                            CurrencyExchangeRate(
//                                    rate.sourceSymbol, rate.destSymbol, rate.rate
//                            )
//                        }
//        )

        return currencyGraph.getRatesV2()
    }


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
    suspend fun fetchRates(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rates {
//        return CurrencyExchangeRatesResponse(
//                currencyGraph.getRates()
//                        .filter { rate ->
//                            (sourceSymbol == "all" || rate.sourceSymbol == sourceSymbol)
//                                    && (destSymbol == "all" || rate.destSymbol == destSymbol)
//                        }
//                        .map { rate ->
//                            CurrencyExchangeRate(
//                                    rate.sourceSymbol, rate.destSymbol, rate.rate
//                            )
//                        }
//        )

        return currencyGraph.getRatesV2(sourceSymbol, destSymbol)
    }


    @PostMapping("/forbidden-pairs")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun addForbiddenPair(@RequestBody request: CurrencyPair) {
        // currencyGraph.addForbiddenRateNames(request.sourceSymbol, request.destSymbol)
        currencyGraph.addForbiddenRateNamesV2(request.sourceSymbol, request.destSymbol)
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun deleteForbiddenPair(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): ForbiddenPairs {
//        currencyGraph.removeForbiddenRateNames(sourceSymbol, destSymbol)
        return currencyGraph.removeForbiddenRateNamesV2(sourceSymbol, destSymbol)
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

        return currencyGraph.getForbiddenRateNamesV2()
    }


























    @PostMapping("/transitive-symbols")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    fun addTransitiveSymbols(@RequestBody symbols: List<String>) {
        currencyGraph.addTransitiveSymbols(symbols)
    }

    @DeleteMapping("/transitive-symbols/{symbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    fun deleteTransitiveSymbols(@PathVariable symbol: String) {
        currencyGraph.removeTransitiveSymbols(listOf(symbol))
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