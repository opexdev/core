package co.nilin.opex.wallet.core.spi


import co.nilin.opex.wallet.core.model.otc.*

interface AuthProxy {
   suspend fun getToken(loginRequest: LoginRequest):LoginResponse
}