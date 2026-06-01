package co.nilin.opex.auth.controller

import co.nilin.opex.auth.data.SessionRequest
import co.nilin.opex.auth.data.Sessions
import co.nilin.opex.auth.service.ForgetPasswordService
import co.nilin.opex.auth.service.LogoutService
import co.nilin.opex.auth.service.SessionService
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
@RequestMapping("/v1/user")
@Tag(
    name = "Auth Gateway - Sessions",
    description = "Authenticated user session and logout operations."
)
class SessionController(
    private val forgetPasswordService: ForgetPasswordService,
    private val logoutService: LogoutService,
    private val sessionService: SessionService
) {

    @PostMapping("/logout")
    @Operation(
        summary = "Logout current session",
        description = """POST /v1/user/logout.
Security: Bearer user-token required.

Behavior: Terminates the current session using the `sid` claim from the JWT.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun logout(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val userId = securityContext.jwtAuthentication().name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        logoutService.logout(userId, sid)
    }

    @PostMapping("/session")
    @Operation(
        summary = "List user sessions",
        description = """POST /v1/user/session.
Security: Bearer user-token required.

Behavior: Returns user sessions for the authenticated user. `uuid` in the request body is overwritten by the authenticated user id.
Allowed values:
- os: ANDROID, IOS, MOBILE_WEB, DESKTOP_WEB
- status: ACTIVE, EXPIRED, TERMINATED.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = Sessions::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getSessions(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody sessionRequest: SessionRequest
    ): List<Sessions> {
        val uuid = securityContext.authentication.name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        sessionRequest.uuid = uuid
        return sessionService.fetchSessions(sessionRequest, sid)
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(
        summary = "Logout one session",
        description = """DELETE /v1/user/session/{sessionId}.
Security: Bearer user-token required.

Behavior: Terminates one user session by id.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "sessionId",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Session id to terminate."
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun logout(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @PathVariable sessionId: String
    ) {
        val uuid = securityContext.authentication.name
        logoutService.logoutSession(uuid, sessionId)
    }

    @PostMapping("/session/delete-others")
    @Operation(
        summary = "Logout other sessions",
        description = """POST /v1/user/session/delete-others.
Security: Bearer user-token required.

Behavior: Terminates all sessions except the current JWT session.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun logoutOthers(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val uuid = securityContext.authentication.name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        logoutService.logoutOthers(uuid, sid)
    }

    @PostMapping("/session/delete-all")
    @Operation(
        summary = "Logout all sessions",
        description = """POST /v1/user/session/delete-all.
Security: Bearer user-token required.

Behavior: Terminates all sessions for the authenticated user.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun logoutAll(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val uuid = securityContext.authentication.name
        logoutService.logoutAll(uuid)
    }
}
