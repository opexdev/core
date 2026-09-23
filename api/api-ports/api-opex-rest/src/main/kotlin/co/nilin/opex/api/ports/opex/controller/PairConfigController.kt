package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.PairConfigResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/market")
@Tag(
    name = "Pair Config",
    description = "Pair configuration endpoints exposed by API."
)
class PairConfigController(
    private val accountantProxy: AccountantProxy
) {
    @GetMapping("/pair/config")
    @Operation(
        summary = "Get all pair configs",
        description = """GET /pair/config.
Security: Public endpoint. No Bearer token is required.

Source:
- Proxies accountant service endpoint: /opex/v1/market/pair/config.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = PairConfigResponse::class))
                )]
            )
        ]
    )
    suspend fun getAllPairConfigs(): List<PairConfigResponse> {
        return accountantProxy.getPairConfigs()
    }
}
