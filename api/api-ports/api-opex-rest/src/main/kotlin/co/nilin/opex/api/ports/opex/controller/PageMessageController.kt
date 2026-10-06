package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.LocalizedPageMessage
import co.nilin.opex.api.core.inout.UiPage
import co.nilin.opex.api.core.spi.ConfigProxy
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1")
@Tag(name = "Page Messages", description = "Public localized page message operations.")
class PageMessageController(private val configProxy: ConfigProxy) {

    @GetMapping("/page-messages", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get page messages",
        description = """GET /opex/v1/page-messages.
Security: Public endpoint. No Bearer token is required.
Behavior: Returns the localized messages in the caller's language. When `page` is omitted, returns the messages for all pages. When `page` is provided, returns a single-element list for that page, or 404 if no message is configured for it.
Allowed values:
- page: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = LocalizedPageMessage::class))
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "`page` was provided and no message is configured for it. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getMessages(
        @Parameter(
            name = "page",
            description = "UI page. Allowed values: MAIN, DASHBOARD, WALLET, LOGIN, REGISTER, SWAP, ADVANCE_MARKET.",
            required = false
        )
        @RequestParam(required = false) page: UiPage?
    ): List<LocalizedPageMessage> {
        val messages = configProxy.getLocalizedPageMessages(
            page,
            UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()
        )
        return messages
    }
}
