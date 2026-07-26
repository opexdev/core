package co.nilin.opex.wallet.core.spi


import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.model.otc.LoginResponse

interface AuthProxy {
    suspend fun getToken(loginRequest: LoginRequest): LoginResponse
}