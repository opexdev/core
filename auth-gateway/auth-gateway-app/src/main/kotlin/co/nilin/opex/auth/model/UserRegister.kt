package co.nilin.opex.auth.model

import com.fasterxml.jackson.annotation.JsonInclude

data class RegisterUserRequest(
    val username: String,
    val password: String,
    val otpCode: String?,
    val otpTracingCode: String?,
    val firstName: String? = null,
    val lastName: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegisterUserResponse(
    val username: String?,
    val otpTracingCode: String?,
)

data class GenericOTPResponse(
    val tracingCode: String?
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
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val attributes: Map<String, List<String>>?
)

data class ForgetPasswordRequest(
    val username: String,
    val newPassword: String,
    val newPasswordConfirmation: String,
    val otpCode: String?,
    val otpTracingCode: String?,
)