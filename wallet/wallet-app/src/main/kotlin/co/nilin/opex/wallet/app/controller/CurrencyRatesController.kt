package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.core.spi.CurrencyService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/otc")
class CurrencyRatesController(private val currencyServiceSpi: CurrencyService,
                              private val graphService: co.nilin.opex.wallet.core.service.otc.GraphService,
                              private val currencyService: co.nilin.opex.wallet.app.service.otc.CurrencyService
) {

    @Autowired
    lateinit var currencyGraph: co.nilin.opex.wallet.app.service.otc.GraphService

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody request: CurrencyImp): Currency? {
        return currencyService.addCurrency(request)
    }

    @PutMapping("/currency")
    suspend fun updateCurrency(@RequestBody request: CurrencyImp): Currency? {
        return currencyService.updateCurrency(request)
    }

    @GetMapping("/currency/{symbol}")
    suspend fun getCurrency(@PathVariable("symbol") symbol: String): Currency? {
        return currencyService.fetchCurrency(symbol)
    }

    @GetMapping("/currency")
    suspend fun getCurrencies(): Currencies? {
        return currencyService.fetchCurrencies()
    }



    @PostMapping("/rate")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun createRate(@RequestBody request: SetCurrencyExchangeRateRequest) {
        request.validate()
        graphService.addRate(Rate(request.sourceSymbol, request.destSymbol, request.rate))
    }


    @PutMapping("/rate")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun updateRate(@RequestBody request: SetCurrencyExchangeRateRequest):Rates {
        request.validate()
      return  graphService.updateRate(Rate(request.sourceSymbol, request.destSymbol, request.rate))
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun deleteRate(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rates {
        return graphService.deleteRate(Rate(sourceSymbol, destSymbol, BigDecimal.ZERO))
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

        return graphService.getRates()
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
    suspend fun fetchRates(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rate? {
        return graphService.getRates(sourceSymbol, destSymbol)
    }


    @PostMapping("/forbidden-pairs")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun addForbiddenPair(@RequestBody request: CurrencyPair) {
        request.validate()
        graphService.addForbiddenPair(ForbiddenPair(request.sourceSymbol, request.destSymbol))
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun deleteForbiddenPair(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): ForbiddenPairs {
        return graphService.deleteForbiddenPair(ForbiddenPair(sourceSymbol, destSymbol))
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

        return graphService.getForbiddenPairs()
    }



    @PostMapping("/transitive-symbols")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun addTransitiveSymbols(@RequestBody symbols: Symbols) {
        graphService.addTransitiveSymbols(symbols)
    }

    @DeleteMapping("/transitive-symbols")
    @ApiResponse(
            message = "OK",
            code = 200,
    )
    suspend fun deleteTransitiveSymbols(@RequestBody symbols: Symbols):Symbols {
     return   graphService.deleteTransitiveSymbols(symbols)
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
        return graphService.getTransitiveSymbols()
    }




    @PostMapping("/route")
    suspend fun fetchRoutes(@RequestParam("sourceSymbol")  sourceSymbol:String?=null,
                            @RequestParam("destSymbol")  destSymbol:String?=null): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(
                currencyGraph.buildRoutes(sourceSymbol,destSymbol).map {  CurrencyExchangeRate(it.getSourceSymbol(), it.getDestSymbol(), it.getRate()) }
        )
    }



}