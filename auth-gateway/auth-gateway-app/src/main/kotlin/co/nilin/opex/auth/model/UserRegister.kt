package co.nilin.opex.auth.model

data class RegisterUserRequest(
    // One of these must be sent
    val username: String,
    val password: String,

    val otpCode: String,
    val otpType: String,
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
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val attributes: Map<String, List<String>>
)

data class Attribute(
    val key: String,
    val value: String
)

enum class UsernameType {
    MOBILE, EMAIL, UNKNOWN;

    fun isUnknown() = this == UNKNOWN
}