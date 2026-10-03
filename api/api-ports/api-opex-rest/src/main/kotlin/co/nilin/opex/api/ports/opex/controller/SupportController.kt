package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.MessageDto
import co.nilin.opex.api.core.inout.TicketDetailDto
import co.nilin.opex.api.core.inout.TicketListResponse
import co.nilin.opex.api.core.inout.TicketSubjectsResponse
import co.nilin.opex.api.core.spi.StorageProxy
import co.nilin.opex.api.core.spi.SupportProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/opex/v1/support")
@Tag(name = "Support", description = "Authenticated user support ticket operations.")
class SupportController(
    private val supportProxy: SupportProxy,
    private val storageProxy: StorageProxy,
    @Value("\${app.support.attachments-bucket}")
    private val attachmentsBucket: String
) {

    @GetMapping("/tickets", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List tickets",
        description = """GET /opex/v1/support/tickets.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns the caller's own tickets, paginated by offset/limit.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TicketListResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun list(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "offset", description = "Pagination offset.", required = false)
        @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(name = "limit", description = "Page size.", required = false)
        @RequestParam(defaultValue = "20") limit: Int
    ): TicketListResponse {
        return supportProxy.getUserTickets(securityContext.jwtAuthentication().tokenValue(), offset, limit)
    }

    @PostMapping("/tickets", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Create ticket",
        description = """POST /opex/v1/support/tickets.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Multipart create. `subjectCode` and `message` are required. `files` is optional and may contain multiple attachments.""",
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
            )
        ]
    )
    suspend fun create(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "subjectCode", description = "Support subject code.", required = true)
        @RequestPart("subjectCode") subjectCode: String,
        @Parameter(name = "message", description = "Initial message body.", required = true)
        @RequestPart("message") message: String,
        @Parameter(name = "files", description = "Optional attachments.", required = false)
        @RequestPart(value = "files", required = false) files: Flux<FilePart>
    ): TicketDetailDto {
        return supportProxy.createTicket(securityContext.jwtAuthentication().tokenValue(), subjectCode, message, files)
    }

    @GetMapping("/tickets/{ticketId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get ticket",
        description = """GET /opex/v1/support/tickets/{ticketId}.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns the ticket detail if it belongs to the caller.""",
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
            )
        ]
    )
    suspend fun get(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketId", description = "Ticket number.", required = true)
        @PathVariable ticketId: String
    ): TicketDetailDto {
        return supportProxy.getTicket(securityContext.jwtAuthentication().tokenValue(), ticketId)
    }

    @PostMapping(
        "/tickets/{ticketId}/messages",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Add message",
        description = """POST /opex/v1/support/tickets/{ticketId}/messages.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Multipart reply. `body` is required. `files` is optional and may contain multiple attachments.""",
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
            )
        ]
    )
    suspend fun addMessage(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketId", description = "Ticket number.", required = true)
        @PathVariable ticketId: String,
        @Parameter(name = "body", description = "Message body.", required = true)
        @RequestPart("body") body: String,
        @Parameter(name = "files", description = "Optional attachments.", required = false)
        @RequestPart(value = "files", required = false) files: Flux<FilePart>
    ): MessageDto {
        return supportProxy.addUserMessage(securityContext.jwtAuthentication().tokenValue(), ticketId, body, files)
    }

    @PostMapping("/tickets/{ticketId}/close", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Close ticket",
        description = """POST /opex/v1/support/tickets/{ticketId}/close.
Security: Bearer user-token required. Requires authenticated user JWT.""",
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
            )
        ]
    )
    suspend fun close(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "ticketId", description = "Ticket number.", required = true)
        @PathVariable ticketId: String
    ): TicketDetailDto {
        return supportProxy.closeTicket(securityContext.jwtAuthentication().tokenValue(), ticketId)
    }

    @GetMapping("/subjects", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List subjects",
        description = """GET /opex/v1/support/subjects.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns the active support subjects for the requested language, defaulting to the caller's language when omitted.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TicketSubjectsResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun subjects(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): TicketSubjectsResponse {
        return supportProxy.getSubjects(securityContext.jwtAuthentication().tokenValue(), UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull()).toString())
    }

    @GetMapping("/attachments")
    @Operation(
        summary = "Download attachment",
        description = """GET /opex/v1/support/attachments.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Downloads a ticket attachment identified by key. Returns binary file bytes. The caller must own the attachment or hold ROLE_admin.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/octet-stream",
                    schema = Schema(type = "string", format = "binary")
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Caller does not own the attachment and lacks ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun downloadAttachment(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @Parameter(name = "key", description = "Storage object key.", required = true)
        @RequestParam("key") key: String
    ): ResponseEntity<ByteArray> {
        return storageProxy.ownerDownload(securityContext.jwtAuthentication().tokenValue(), attachmentsBucket, key)
    }
}
