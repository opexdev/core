package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.UpdateEmailRequest
import co.nilin.opex.auth.model.UpdateMobileRequest
import co.nilin.opex.auth.model.UpdateNameRequest
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
        val userId = securityContext.jwtAuthentication().name
        val sid = securityContext.jwtAuthentication().tokenAttributes["sid"] as String?
            ?: throw OpexError.InvalidToken.exception()
        userService.logout(userId, sid)
    }

    @PutMapping("/update/email")
    suspend fun updateEmail(@RequestBody request: UpdateEmailRequest) {
        userService.updateEmail(request)
    }

    @PutMapping("/update/mobile")
    suspend fun updateMobile(@RequestBody request: UpdateMobileRequest) {
        userService.updateMobile(request)
    }

    @PutMapping("/update/name")
    suspend fun updateName(@RequestBody request: UpdateNameRequest) {
        userService.updateName(request)
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

