package co.nilin.opex.auth.gateway.controller

import co.nilin.opex.auth.gateway.data.RegisterUserRequest
import co.nilin.opex.auth.gateway.data.RegisterUserResponse
import co.nilin.opex.auth.gateway.service.KeycloakService
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ext/user")
class AuthAdminController(private val service: KeycloakService) {

    @PostMapping
    fun registerUser(@RequestBody request: RegisterUserRequest): RegisterUserResponse {
        if (!request.isValid())
            throw OpexException(OpexError.BadRequest)

        val id = service.registerUser(request)
        return RegisterUserResponse(id)
    }

    @PostMapping("/forgot")
    fun forgotPassword(@RequestParam email: String?) {
        if (email.isNullOrEmpty())
            throw OpexException(OpexError.BadRequest)
        service.forgotPassword(email)
    }

    @PostMapping("/verify-email")
    fun sendVerifyEmail(@RequestParam email: String?) {
        if (email.isNullOrEmpty())
            throw OpexException(OpexError.BadRequest)
        service.sendVerification(email)
    }

}