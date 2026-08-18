package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.ConfirmTwoFactorRequest
import co.nilin.opex.api.core.inout.OTPType
import co.nilin.opex.api.core.inout.SetupTOTPResponse
import co.nilin.opex.api.core.inout.TOTPCode
import co.nilin.opex.api.core.inout.TwoFactorRequest
import co.nilin.opex.api.core.inout.TwoFactorResponse
import co.nilin.opex.api.core.inout.auth.*

interface AuthProxy {

    suspend fun requestGetToken(request: PasswordFlowTokenRequest): TokenResponse
    suspend fun confirmGetToken(request: ConfirmPasswordFlowTokenRequest): TokenResponse
    suspend fun resendLoginOtp(request: ResendOtpRequest, token: String): ResendOtpResponse
    suspend fun getToken(request: ExternalIdpTokenRequest): TokenResponse
    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse
    suspend fun registerUser(request: RegisterUserRequest): TempOtpResponse
    suspend fun verifyRegister(request: VerifyOTPRequest): OTPActionTokenResponse
    suspend fun confirmRegister(request: ConfirmRegisterRequest): Token
    suspend fun registerExternalIdpUser(request: ExternalIdpUserRegisterRequest): TokenResponse
    suspend fun forgetPassword(request: ForgotPasswordRequest): TempOtpResponse
    suspend fun verifyForget(request: VerifyOTPRequest): OTPActionTokenResponse
    suspend fun confirmForget(request: ConfirmForgetRequest)
    suspend fun logout(token: String)
    suspend fun logout(sessionId: String, token: String)
    suspend fun getSessions(request: SessionRequest, token: String): List<Sessions>
    suspend fun logoutOthers(token: String)
    suspend fun logoutAll(token: String)

    suspend fun getTwoFactorConfig(token: String): OTPType
    suspend fun requestEnableTwoFactor(request: TwoFactorRequest, token: String): TwoFactorResponse
    suspend fun confirmEnableTwoFactor(request: ConfirmTwoFactorRequest, token: String): OTPVerifyResponse
    suspend fun requestDisableTwoFactor(request: TwoFactorRequest, token: String): TwoFactorResponse
    suspend fun confirmDisableTwoFactor(request: ConfirmTwoFactorRequest, token: String): OTPVerifyResponse
    suspend fun setupTOTP(token: String): SetupTOTPResponse
    suspend fun verifyTOTPSetup(request: TOTPCode, token: String)

}