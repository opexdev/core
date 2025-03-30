package co.nilin.opex.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterUserRequest(
    @field:NotNull
    val otpVerifyRequest: OTPVerifyRequest,

    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    val firstName: String? = null,
    val lastName: String? = null
)

data class ExternalIdpUserRegisterRequest(
    val idToken: String,
    val idp: String,
    val password: String,
    val otpVerifyRequest: OTPVerifyRequest?
)

data class KeycloakUser(
    val id: String,
    val username: String,
    val email: String
)