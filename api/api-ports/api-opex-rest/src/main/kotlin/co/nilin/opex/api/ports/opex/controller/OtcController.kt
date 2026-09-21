package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.pricemanagement.SparkLineView
import co.nilin.opex.api.core.spi.PriceManagementProxy
import co.nilin.opex.common.OpexError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/otc")
class OtcController(private val priceManagementProxy: PriceManagementProxy) {

    enum class Period(val code: String) {
        DAILY("24h"),
        WEEKLY("7d"),
        MONTHLY("1M");

        companion object {
            fun fromCode(code: String): Period? {
                return entries.find { it.code == code }
            }
        }
    }

    @GetMapping("/spark-line")
    @Operation(
        tags = ["OTC Sparklines"],
        summary = "Get OTC sparklines for a reference currency",
        description = """Returns a sparkline for every base asset price-management can price against `refCurrency`.

Authentication:
- Public endpoint. No Bearer token is required.

Behavior:
- Proxies price-management. Returns a sparkline for every base asset it can price against
  `refCurrency` (e.g. `refCurrency=USDT` -> BTC-USDT, ETH-USDT, ...), reconstructing pairs it
  does not track directly from other tracked pairs.
- Pairs that cannot be derived are omitted from the response.

Query parameters:

Response body: Array<SparkLineView>.""",
        parameters = [
            Parameter(
                name = "refCurrency",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "Reference currency (the second side of the pairs).",
                example = "USDT",
                schema = Schema(type = "string")
            ),
            Parameter(
                name = "period",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "Sparkline period.",
                example = "24h",
                schema = Schema(type = "string", allowableValues = ["24h", "7d", "1M"])
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Sparklines returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = SparkLineView::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid period. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getSparkLine(
        @RequestParam("refCurrency") refCurrency: String,
        @RequestParam("period") periodCode: String
    ): List<SparkLineView> {
        Period.fromCode(periodCode) ?: throw OpexError.InvalidPriceChangeDuration.exception()
        return priceManagementProxy.getSparkLine(refCurrency, periodCode)
    }
}
