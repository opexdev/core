package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.auth.*
import co.nilin.opex.api.core.spi.AuthProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class AuthProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : AuthProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.auth-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun requestGetToken(request: PasswordFlowTokenRequest): TokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/oauth/protocol/openid-connect/token")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get token") }
    }

    override suspend fun confirmGetToken(request: ConfirmPasswordFlowTokenRequest): TokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/oauth/protocol/openid-connect/token/confirm")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to confirm token") }
    }

    override suspend fun resendLoginOtp(
        request: ResendOtpRequest,
        token: String
    ): ResendOtpResponse {
        return webClient.post()
            .uri("$baseUrl/v1/oauth/protocol/openid-connect/token/resend-otp")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ResendOtpResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to resend otp") }
    }

    override suspend fun getToken(request: ExternalIdpTokenRequest): TokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/oauth/protocol/openid-connect/token-external")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get token") }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/oauth/protocol/openid-connect/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to refresh token") }
    }

    override suspend fun registerUser(request: RegisterUserRequest): TempOtpResponse {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/register")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TempOtpResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to register user") }
    }

    override suspend fun verifyRegister(request: VerifyOTPRequest): OTPActionTokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/register/verify")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<OTPActionTokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to verify register") }
    }

    override suspend fun confirmRegister(request: ConfirmRegisterRequest): Token {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/register/confirm")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Token>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to confirm register") }
    }

    override suspend fun registerExternalIdpUser(request: ExternalIdpUserRegisterRequest): TokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/register-external")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to register user") }
    }

    override suspend fun forgetPassword(request: ForgotPasswordRequest): TempOtpResponse {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/forget")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TempOtpResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to forget password") }
    }

    override suspend fun verifyForget(request: VerifyOTPRequest): OTPActionTokenResponse {
        return webClient.post()
            .uri("$baseUrl/v1/user/public/forget/verify")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<OTPActionTokenResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to verify forget password") }
    }

    override suspend fun confirmForget(request: ConfirmForgetRequest) {
        webClient.post()
            .uri("$baseUrl/v1/user/public/forget/confirm")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun logout(token: String) {
        webClient.post()
            .uri("$baseUrl/v1/user/logout")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun logout(sessionId: String, token: String) {
        webClient.delete()
            .uri("$baseUrl/v1/user/session/$sessionId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getSessions(
        request: SessionRequest,
        token: String
    ): List<Sessions> {
        return webClient.post()
            .uri("$baseUrl/v1/user/session")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<Sessions>>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get sessions") }
    }

    override suspend fun logoutOthers(token: String) {
        webClient.post()
            .uri("$baseUrl/v1/user/session/delete-others")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun logoutAll(token: String) {
        webClient.post()
            .uri("$baseUrl/v1/user/session/delete-all")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }
}