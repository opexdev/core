package co.nilin.opex.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterUserRequest(
    // One of these must be sent
    @field:Email(message = "Invalid email format")
    val email: String?,
    val mobile: String?,

    val password: String,

    val otpCode: String,
    val otpTracingCode: String,

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