package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.PageMessage
import co.nilin.opex.api.core.inout.UiPage
import co.nilin.opex.api.core.inout.UpdatePageMessageRequest
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/page-messages")
@Tag(name = "Page Messages Admin", description = "Admin page message operations.")
class PageMessageAdminController(private val configProxy: ConfigProxy) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List page messages",
        description = """GET /opex/v1/admin/page-messages.
Security: Bearer admin-token required. Required authority: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = PageMessage::class))
                )]
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
    suspend fun listMessages(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<PageMessage> {
        return configProxy.getAllPageMessages(securityContext.jwtAuthentication().tokenValue())
    }

    @GetMapping("/{page}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get page message",
        description = """GET /opex/v1/admin/page-messages/{page}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- page: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = PageMessage::class))]
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
    suspend fun getMessage(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(
            name = "page",
            description = "UI page. Allowed values: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.",
            required = true
        )
        @PathVariable page: UiPage
    ): PageMessage? {
        return configProxy.getPageMessage(securityContext.jwtAuthentication().tokenValue(), page)
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Create or update page message",
        description = """POST /opex/v1/admin/page-messages.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- page: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.
- type: DEFAULT, INFO, WARNING, ERROR, SUCCESS.
- translations keys: EN, FA, AR, UZ.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdatePageMessageRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = PageMessage::class))]
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
    suspend fun createOrUpdateMessage(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UpdatePageMessageRequest
    ): PageMessage {
        return configProxy.createOrUpdatePageMessage(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @DeleteMapping("/{page}")
    @Operation(
        summary = "Delete page message",
        description = """DELETE /opex/v1/admin/page-messages/{page}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Allowed values:
- page: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "No response body.", content = [Content()]),
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
    suspend fun deleteMessage(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(
            name = "page",
            description = "UI page. Allowed values: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.",
            required = true
        )
        @PathVariable page: UiPage
    ) {
        configProxy.deletePageMessage(securityContext.jwtAuthentication().tokenValue(), page)
    }
}
