package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AdminTicketListResponse
import co.nilin.opex.api.core.inout.ConversationStatus
import co.nilin.opex.api.core.inout.MessageDto
import co.nilin.opex.api.core.inout.TicketDetailDto
import co.nilin.opex.api.core.spi.SupportProxy
import co.nilin.opex.api.ports.opex.service.OwnerNameResolver
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import co.nilin.opex.api.ports.opex.util.withSenderNames
import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/opex/v1/admin/support")
@Tag(name = "Support Admin", description = "Admin support ticket operations.")
class SupportAdminController(
    private val supportProxy: SupportProxy,
    private val ownerNameResolver: OwnerNameResolver
) {

    private suspend fun resolveLanguage(): String =
        UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString()

    @GetMapping("/tickets", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List tickets",
        description = """GET /opex/v1/admin/support/tickets.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns tickets across all users, optionally filtered by status, userId, ticketNumber and/or subjectCode, paginated by offset/limit.
Allowed values:
- status: WAITING_FOR_ADMIN, WAITING_FOR_USER, CLOSED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AdminTicketListResponse::class))]
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
    suspend fun listTickets(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "status", description = "Filter by ticket status. Allowed values: WAITING_FOR_ADMIN, WAITING_FOR_USER, CLOSED.", required = false)
        @RequestParam status: ConversationStatus?,
        @Parameter(name = "userId", description = "Filter by owning user id.", required = false)
        @RequestParam userId: String?,
        @Parameter(name = "ticketNumber", description = "Filter by ticket number.", required = false)
        @RequestParam(required = false) ticketNumber: String?,
        @Parameter(name = "subjectCode", description = "Filter by support subject code.", required = false)
        @RequestParam(required = false) subjectCode: String?,
        @Parameter(name = "offset", description = "Pagination offset.", required = false)
        @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(name = "limit", description = "Page size.", required = false)
        @RequestParam(defaultValue = "20") limit: Int
    ): AdminTicketListResponse {
        return supportProxy.getAdminTickets(
            securityContext.jwtAuthentication().tokenValue(),
            status,
            userId,
            offset,
            limit,
            resolveLanguage(),
            ticketNumber,
            subjectCode
        )
    }

    @GetMapping("/tickets/{ticketNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get ticket",
        description = """GET /opex/v1/admin/support/tickets/{ticketNumber}.
Security: Bearer admin-token required. Required authority: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TicketDetailDto::class))]
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
    suspend fun getTicket(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketNumber", description = "Ticket number.", required = true)
        @PathVariable ticketNumber: String
    ): TicketDetailDto {
        val token = securityContext.jwtAuthentication().tokenValue()
        val ticket = supportProxy.getAdminTicket(token, ticketNumber, resolveLanguage())
        return ownerNameResolver.withSenderNames(token, ticket)
    }

    @PostMapping(
        "/tickets/{ticketNumber}/messages",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Add message",
        description = """POST /opex/v1/admin/support/tickets/{ticketNumber}/messages.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Multipart reply. `body` is optional when at least one file is attached. `files` is optional and may contain multiple attachments.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = MessageDto::class))]
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
    suspend fun addTicketMessage(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketNumber", description = "Ticket number.", required = true)
        @PathVariable ticketNumber: String,
        @Parameter(name = "body", description = "Message body.", required = false)
        @RequestPart(value = "body", required = false) body: String?,
        @Parameter(name = "files", description = "Optional attachments.", required = false)
        @RequestPart(value = "files", required = false) files: Flux<FilePart>
    ): MessageDto {
        val token = securityContext.jwtAuthentication().tokenValue()
        val message = supportProxy.addAgentTicketMessage(token, ticketNumber, body, files)
        return ownerNameResolver.withSenderNames(token, message)
    }

    @PostMapping("/tickets/{ticketNumber}/close", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Close ticket",
        description = """POST /opex/v1/admin/support/tickets/{ticketNumber}/close.
Security: Bearer admin-token required. Required authority: ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TicketDetailDto::class))]
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
    suspend fun closeTicket(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketNumber", description = "Ticket number.", required = true)
        @PathVariable ticketNumber: String
    ): TicketDetailDto {
        val token = securityContext.jwtAuthentication().tokenValue()
        val ticket = supportProxy.closeAdminTicket(token, ticketNumber, resolveLanguage())
        return ownerNameResolver.withSenderNames(token, ticket)
    }
}
