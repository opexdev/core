package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransferPreEvaluateResponse
import co.nilin.opex.wallet.app.dto.TransferReserveRequest
import co.nilin.opex.wallet.app.dto.TransferReserveResponse
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*

@RestController
class AdvancedTransferController {

    @GetMapping("/v3/amount/{amount}_{symbol}/{destSymbol}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"destAmount\": \"111\"}",
                mediaType = "application/json"
            )
        )
    )
    suspend fun calculateDestinationAmount(
        @PathVariable("symbol") symbol: String,
        @PathVariable("amount") amount: BigDecimal,
        @PathVariable("symbol") destSymbol: String,
    ): TransferPreEvaluateResponse {
        return TransferPreEvaluateResponse(amount.multiply(BigDecimal.TEN))
    }

    @PostMapping("/v3/transfer/reserve")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"reserveUuid\": \"214234\"," +
                        "  \"guaranteedDestAmount\": \"1000\"}",
                mediaType = "application/json"
            )
        )
    )
    suspend fun reserve(
        @RequestBody request: TransferReserveRequest
    ): TransferReserveResponse {
        return TransferReserveResponse(UUID.randomUUID().toString(), BigDecimal.ONE)
    }

    @PostMapping("/v3/transfer/{reserveUuid}")
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{}",
                mediaType = "application/json"
            )
        )
    )
    suspend fun finalizeTransfer(
        @PathVariable("reserveUuid") reserveUuid: String
    ): TransferResult {
        return TransferResult(
            System.currentTimeMillis(),
            UUID.randomUUID().toString(), "main",
            Amount(Currency("BTC", "BTC", BigDecimal.ONE), BigDecimal.ONE),
            Amount(Currency("BTC", "BTC", BigDecimal.ONE), BigDecimal.ONE),
            Amount(Currency("BTC", "BTC", BigDecimal.ONE), BigDecimal.ONE),
            UUID.randomUUID().toString(), "main",
            Amount(Currency("ETH", "ETH", BigDecimal.TEN), BigDecimal.ONE)
        )
    }
}