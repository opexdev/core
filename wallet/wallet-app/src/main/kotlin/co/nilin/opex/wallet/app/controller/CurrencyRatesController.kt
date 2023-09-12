package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrencyExchangeRate
import co.nilin.opex.wallet.app.dto.CurrencyExchangeRatesResponse
import co.nilin.opex.wallet.app.dto.SetCurrencyExchangeRateRequest
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class CurrencyRatesController {

    @GetMapping("/rates/{sourceSymbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"rates\": [{\"sourceSymbol\": \"BTC\",\n" +
                        "               \"destSymbol\": \"ETH\",\n" +
                        " \"rate\": \"100.0\",\n" +
                        " \"fee\": \"1.0\"}] }",
                mediaType = "application/json"
            )
        )
    )
    fun fetchRates(sourceSymbol: String, destSymbol: String): CurrencyExchangeRatesResponse {
        return CurrencyExchangeRatesResponse(
            listOf(
                CurrencyExchangeRate(
                    "BTC", "ETH",
                    BigDecimal.valueOf(1000), BigDecimal.ONE
                )
            )
        )
    }

    @PostMapping("/rates")
    @ApiResponse(
        message = "OK",
        code = 200,
    )
    fun updateOrCreateRate(@RequestBody request: SetCurrencyExchangeRateRequest) {

    }
}