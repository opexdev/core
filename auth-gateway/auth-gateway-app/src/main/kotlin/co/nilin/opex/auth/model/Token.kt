package co.nilin.opex.auth.model

import co.nilin.opex.auth.data.Device
import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordFlowTokenRequest(
    val username: String,
    val password: String,
    val clientId: String,
    val clientSecret: String?,
    val rememberMe: Boolean = true,
    val captchaType: CaptchaType? = CaptchaType.INTERNAL,
    val captchaCode: String?,
):Device()

data class ConfirmPasswordFlowTokenRequest(
    val username: String,
    val token: String,
    val clientId: String,
    val clientSecret: String?,
    val otp: String,
    val rememberMe: Boolean = true,
): Device()

data class ResendOtpRequest(
    val username: String,
    val clientId: String
)


data class RefreshTokenRequest(
    val clientId: String,
    val clientSecret: String?,
    val refreshToken: String
):Device()

data class ExternalIdpTokenRequest(
    val idToken: String,
    val accessToken: String,
    val idp: String,
    val otpVerifyRequest: OTPVerifyRequest?
):Device()

data class Token(
    @JsonProperty("access_token")
    val accessToken: String,          // The access token

    @JsonProperty("expires_in")
    val expiresIn: Int,               // Expiration time of the access token in seconds

    @JsonProperty("refresh_expires_in")
    var refreshExpiresIn: Int?,        // Expiration time of the refresh token in seconds

    @JsonProperty("refresh_token")
    var refreshToken: String?,         // The refresh token

    @JsonProperty("token_type")
    val tokenType: String?,            // Type of token (usually "Bearer")

    @JsonProperty("not-before-policy")
    val notBeforePolicy: Int?,         // Timestamp indicating when the token becomes valid

    @JsonProperty("session_state")
    val sessionState: String?,         // Session state (optional)

    @JsonProperty("scope")
    val scope: String?                 // Scopes associated with the token

)

data class TokenResponse(
    val token: Token?,
    val otp: RequiredOTP?,
    //TODO IMPORTANT: remove in production
    val otpCode: String?,
)

data class RequiredOTP(
    val type: OTPType,
    val receiver: String?
)

data class ResendOtpResponse(
    val otp: RequiredOTP?,
    //TODO IMPORTANT: remove in production
    val otpCode: String?,
)