package co.nilin.opex.auth.model

data class RegisterUserRequest(
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null
)

data class VerifyOTPRequest(
    val username: String,
    val otp: String
)

data class OTPActionTokenResponse(
    val token: String
)

data class ConfirmRegisterRequest(
    val password: String,
    val token: String,
    val clientId: String?,
    val clientSecret: String?
)

data class TokenData(
    val isValid: Boolean,
    val userId: String,
    val action: OTPAction
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
    val emailVerified: Boolean,
    val enabled: Boolean,
    val attributes: Map<String, List<String>>?
)

data class ForgetPasswordRequest(
    val username: String,
    val otpCode: String?,
    val otpTracingCode: String?,
)

data class ConfirmForgetRequest(
    val newPassword: String,
    val newPasswordConfirmation: String,
    val token: String
)