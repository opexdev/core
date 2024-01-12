package co.nilin.opex.bcgateway.core.spi


import co.nilin.opex.bcgateway.core.model.otc.*

interface AuthProxy {
   suspend fun getToken(loginRequest: LoginRequest):LoginResponse
}