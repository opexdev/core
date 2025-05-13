package co.nilin.opex.auth.controller

import co.nilin.opex.auth.service.UserService
import co.nilin.opex.auth.utils.jwtAuthentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user")
class UserController(private val userService: UserService) {

    @PostMapping("/logout")
    suspend fun logout(@CurrentSecurityContext securityContext: SecurityContext) {
        userService.logout(securityContext.jwtAuthentication().name)
    }

}

