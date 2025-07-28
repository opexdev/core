package co.nilin.opex.auth.controller

import co.nilin.opex.auth.data.ActiveSession
import co.nilin.opex.auth.service.UserService
import co.nilin.opex.auth.utils.jwtAuthentication
import co.nilin.opex.common.OpexError
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user")
class UserController(private val userService: UserService) {

    @PostMapping("/logout")
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext) {
        userService.logout(securityContext.jwtAuthentication().name)
    }

    @GetMapping("/session")
    suspend fun getSessions(@CurrentSecurityContext securityContext: SecurityContext): List<ActiveSession> {
        val uuid = securityContext.authentication.name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        return userService.fetchActiveSessions(uuid, sid)
    }

    @DeleteMapping("/session/{sessionId}")
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext, @PathVariable sessionId: String) {
        val uuid = securityContext.authentication.name
        userService.logoutSession(uuid, sessionId)
    }

    @PostMapping("/session/delete-others")
    suspend fun logoutOthers(@CurrentSecurityContext securityContext: SecurityContext) {
        val uuid = securityContext.authentication.name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        userService.logoutOthers(uuid, sid)
    }

    @PostMapping("/session/delete-all")
    suspend fun logoutAll(@CurrentSecurityContext securityContext: SecurityContext) {
        val uuid = securityContext.authentication.name
        userService.logoutAll(uuid)
    }

}

