package co.nilin.opex.auth.controller

import co.nilin.opex.auth.data.SessionRequest
import co.nilin.opex.auth.data.Sessions
import co.nilin.opex.auth.service.ForgetPasswordService
import co.nilin.opex.auth.service.LogoutService
import co.nilin.opex.auth.service.SessionService
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.jwtAuthentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user")
class SessionController(
    private val forgetPasswordService: ForgetPasswordService,
    private val logoutService: LogoutService,
    private val sessionService: SessionService
) {

    @PostMapping("/logout")
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext) {
        val userId = securityContext.jwtAuthentication().name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        logoutService.logout(userId, sid)
    }

    @PostMapping("/session")
    suspend fun getSessions(
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
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext, @PathVariable sessionId: String) {
        val uuid = securityContext.authentication.name
        logoutService.logoutSession(uuid, sessionId)
    }

    @PostMapping("/session/delete-others")
    suspend fun logoutOthers(@CurrentSecurityContext securityContext: SecurityContext) {
        val uuid = securityContext.authentication.name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        logoutService.logoutOthers(uuid, sid)
    }

    @PostMapping("/session/delete-all")
    suspend fun logoutAll(@CurrentSecurityContext securityContext: SecurityContext) {
        val uuid = securityContext.authentication.name
        logoutService.logoutAll(uuid)
    }

}

