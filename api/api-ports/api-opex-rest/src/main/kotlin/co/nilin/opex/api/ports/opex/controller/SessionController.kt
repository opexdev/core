package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.auth.SessionRequest
import co.nilin.opex.api.core.inout.auth.Sessions
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.jwtAuthentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/user")
@Tag(
    name = "Auth Gateway - Sessions", description = "Authenticated user session and logout operations."
)
class SessionController(private val authProxy: AuthProxy) {

    @PostMapping("/logout")
    @Operation(
        summary = "Logout current session", description = """POST /opex/v1/user/logout.
Security: Bearer user-token required.

Behavior: Terminates the current session using the `sid` claim from the JWT.
Response body: No response body.""", security = [SecurityRequirement(name = "bearerAuth")], responses = [ApiResponse(
            responseCode = "200", description = "Successful response. No response body.", content = [Content()]
        ), ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
            content = [Content()]
        )]
    )
    suspend fun logout(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val userId = securityContext.jwtAuthentication().name
        authProxy.logout(userId)
    }

    @PostMapping("/session")
    @Operation(
        summary = "List user sessions",
        description = """POST /opex/v1/user/session.
Security: Bearer user-token required.

Behavior: Returns user sessions for the authenticated user. `uuid` in the request body is overwritten by the authenticated user id.
Allowed values:
- os: ANDROID, IOS, MOBILE_WEB, DESKTOP_WEB
- status: ACTIVE, EXPIRED, TERMINATED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [ApiResponse(
            responseCode = "200", description = "Successful response.", content = [Content(
                mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = Sessions::class))
            )]
        ), ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
            content = [Content()]
        )]
    )
    suspend fun getSessions(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody sessionRequest: SessionRequest
    ): List<Sessions> {
        val uuid = securityContext.authentication.name
        sessionRequest.uuid = uuid
        return authProxy.getSessions(sessionRequest, uuid)
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(
        summary = "Logout one session", description = """DELETE /opex/v1/user/session/{sessionId}.
Security: Bearer user-token required.

Behavior: Terminates one user session by id.
Response body: No response body.""", security = [SecurityRequirement(name = "bearerAuth")], parameters = [Parameter(
            name = "sessionId", `in` = ParameterIn.PATH, required = true, description = "Session id to terminate."
        )], responses = [ApiResponse(
            responseCode = "200", description = "Successful response. No response body.", content = [Content()]
        ), ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
            content = [Content()]
        )]
    )
    suspend fun logout(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sessionId: String
    ) {
        val uuid = securityContext.authentication.name
        authProxy.logout(uuid, sessionId)
    }

    @PostMapping("/session/delete-others")
    @Operation(
        summary = "Logout other sessions", description = """POST /opex/v1/user/session/delete-others.
Security: Bearer user-token required.

Behavior: Terminates all sessions except the current JWT session.
Response body: No response body.""", security = [SecurityRequirement(name = "bearerAuth")], responses = [ApiResponse(
            responseCode = "200", description = "Successful response. No response body.", content = [Content()]
        ), ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
            content = [Content()]
        )]
    )
    suspend fun logoutOthers(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val uuid = securityContext.authentication.name
        authProxy.logoutOthers(uuid)
    }

    @PostMapping("/session/delete-all")
    @Operation(
        summary = "Logout all sessions", description = """POST /opex/v1/user/session/delete-all.
Security: Bearer user-token required.

Behavior: Terminates all sessions for the authenticated user.
Response body: No response body.""", security = [SecurityRequirement(name = "bearerAuth")], responses = [ApiResponse(
            responseCode = "200", description = "Successful response. No response body.", content = [Content()]
        ), ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
            content = [Content()]
        )]
    )
    suspend fun logoutAll(
        @Parameter(hidden = true) @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val uuid = securityContext.authentication.name
        authProxy.logoutAll(uuid)
    }
}
