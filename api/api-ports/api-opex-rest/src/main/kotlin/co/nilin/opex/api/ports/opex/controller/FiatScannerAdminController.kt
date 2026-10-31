package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.PodiumStatementsRequest
import co.nilin.opex.api.core.spi.FiatScannerProxy
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/fiat-scanner")
@Tag(
    name = "Fiat Scanner Admin",
    description = "Admin access to the fiat-scanner subscription service: bank statements and webhook deliveries."
)
class FiatScannerAdminController(
    private val fiatScannerProxy: FiatScannerProxy
) {

    @PostMapping("/podium/statements", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Fetch bank statements from podium",
        description = """POST /opex/v1/admin/fiat-scanner/podium/statements.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `depositNumber`, `startTime` and `endTime` are required. The times are epoch millis. `limit` and `offset` are optional.
Behavior: Asks the fiat-scanner subscription service for the statements of a deposit number, which forwards to podium. The subscription service decides which deposit numbers this platform may read from its own subscriptions, so a deposit number outside them answers 403.
Response: Relayed from upstream as it arrived, body and status code both. The shape is podium's, not this gateway's, so it is passed through rather than remodelled here.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. The body is podium's statement payload.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Either the caller is missing ROLE_admin, or the deposit number is not one this platform may read.",
                content = [Content()]
            )
        ]
    )
    suspend fun fetchPodiumStatements(
        @RequestBody request: PodiumStatementsRequest
    ): ResponseEntity<String> {
        val response = fiatScannerProxy.fetchPodiumStatements(request)
        return ResponseEntity.status(response.status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response.body)
    }

    @GetMapping("/webhooks", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List webhook deliveries",
        description = """GET /opex/v1/admin/fiat-scanner/webhooks.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Lists the webhook calls the notifier made for this platform's deposit numbers. `status`, `limit` and `offset` are all optional and are passed through untouched; left out, upstream applies its own defaults of 100 and 0.
Response: Relayed from upstream as it arrived, body and status code both.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. The body is the notifier's webhook list.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun listWebhooks(
        @Parameter(name = "status", description = "Optional webhook delivery status filter, passed through to the notifier.")
        @RequestParam(required = false) status: String?,
        @Parameter(name = "limit", description = "Optional page size. Upstream defaults to 10.")
        @RequestParam(required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Upstream defaults to 0.")
        @RequestParam(required = false) offset: Int?
    ): ResponseEntity<String> {
        val response = fiatScannerProxy.listWebhooks(status, limit, offset)
        return ResponseEntity.status(response.status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response.body)
    }

    @PostMapping("/webhooks/{id}/retry", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retry a webhook delivery",
        description = """POST /opex/v1/admin/fiat-scanner/webhooks/{id}/retry.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Asks the notifier to deliver a webhook call again. The subscription service checks the call belongs to one of this platform's deposit numbers first.
Response: Relayed from upstream as it arrived, body and status code both.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Either the caller is missing ROLE_admin, or the webhook call is not one this platform may touch.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "No webhook call with this id.", content = [Content()])
        ]
    )
    suspend fun retryWebhook(
        @Parameter(name = "id", description = "Webhook call id.", required = true)
        @PathVariable("id") id: Long
    ): ResponseEntity<String> {
        val response = fiatScannerProxy.retryWebhook(id)
        return ResponseEntity.status(response.status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response.body)
    }
}
