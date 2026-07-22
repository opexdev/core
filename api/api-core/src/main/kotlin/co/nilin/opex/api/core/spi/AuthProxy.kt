package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.auth.*

interface AuthProxy {

    suspend fun requestGetToken(request: PasswordFlowTokenRequest): TokenResponse
    suspend fun confirmGetToken(request: ConfirmPasswordFlowTokenRequest): TokenResponse
    suspend fun resendLoginOtp(request: ResendOtpRequest, uuid: String): ResendOtpResponse
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

}