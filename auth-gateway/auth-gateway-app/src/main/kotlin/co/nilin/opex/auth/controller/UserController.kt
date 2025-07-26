package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.UpdateEmailRequest
import co.nilin.opex.auth.model.UpdateMobileRequest
import co.nilin.opex.auth.model.UpdateNameRequest
import co.nilin.opex.auth.service.UserService
import co.nilin.opex.auth.utils.jwtAuthentication
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

    @PutMapping("/update/email")
    suspend fun updateEmail(
        @RequestBody request: UpdateEmailRequest
    ) {
        userService.updateEmail(request)
    }

    @PutMapping("/update/mobile")
    suspend fun updateMobile(
        @RequestBody request: UpdateMobileRequest
    ) {
        userService.updateMobile(request)

    }

    @PutMapping("/update/name")
    suspend fun updateName(
        @RequestBody request: UpdateNameRequest
    ) {
        userService.updateName(request)
    }
}

