package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyExchangeRate
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.CurrencyPair
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import co.nilin.opex.wallet.app.service.otc.GraphService
import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.CurrencyImp
import co.nilin.opex.wallet.core.model.otc.*
import co.nilin.opex.wallet.app.service.otc.OTCCurrencyService
import co.nilin.opex.wallet.core.service.otc.RateService
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/otc")
class CurrencyRatesController(
    private val rateService: RateService,
    private val OTCCurrencyService: OTCCurrencyService,
    private val graphService: GraphService
) {

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody request: CurrencyImp): Currency? {
        return OTCCurrencyService.addCurrency(request)
    }

    @PutMapping("/currency")
    suspend fun updateCurrency(@RequestBody request: CurrencyImp): Currency? {
        return OTCCurrencyService.updateCurrency(request)
    }

    @GetMapping("/currency/{symbol}")
    suspend fun getCurrency(@PathVariable("symbol") symbol: String): Currency? {
        return OTCCurrencyService.fetchCurrency(symbol)
    }

    @GetMapping("/currency")
    suspend fun getCurrencies(): Currencies? {
        return OTCCurrencyService.fetchCurrencies()
    }

    @PostMapping("/rate")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun createRate(@RequestBody request: SetCurrencyExchangeRateRequest) {
        request.validate()
        rateService.addRate(Rate(request.sourceSymbol, request.destSymbol, request.rate))
    }

    @PutMapping("/rate")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun updateRate(@RequestBody request: SetCurrencyExchangeRateRequest): Rates {
        request.validate()
        return rateService.updateRate(Rate(request.sourceSymbol, request.destSymbol, request.rate))
    }

    @DeleteMapping("/rate/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteRate(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): Rates {
        return rateService.deleteRate(Rate(sourceSymbol, destSymbol, BigDecimal.ZERO))
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
    suspend fun addForbiddenPair(@RequestBody request: CurrencyPair) {
        request.validate()
        rateService.addForbiddenPair(ForbiddenPair(request.sourceSymbol, request.destSymbol))
    }

    @DeleteMapping("/forbidden-pairs/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteForbiddenPair(@PathVariable sourceSymbol: String, @PathVariable destSymbol: String): ForbiddenPairs {
        return rateService.deleteForbiddenPair(ForbiddenPair(sourceSymbol, destSymbol))
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

    @PostMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun addTransitiveSymbols(@RequestBody symbols: Symbols) {
        rateService.addTransitiveSymbols(symbols)
    }

    @DeleteMapping("/transitive-symbols/{symbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteTransitiveSymbols(@PathVariable symbol: String): Symbols {
        return rateService.deleteTransitiveSymbols(Symbols(listOf(symbol)))
    }

    @DeleteMapping("/transitive-symbols")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    suspend fun deleteTransitiveSymbols(@RequestBody symbols: Symbols): Symbols {
        return rateService.deleteTransitiveSymbols(symbols)
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
        return CurrencyExchangeRatesResponse(
            graphService.buildRoutes(sourceSymbol, destSymbol).map { CurrencyExchangeRate(it.getSourceSymbol(), it.getDestSymbol(), it.getRate()) }
        )
    }

}