package co.nilin.opex.bcgateway.core.spi


import co.nilin.opex.bcgateway.core.model.otc.LoginRequest
import co.nilin.opex.bcgateway.core.model.otc.LoginResponse

interface AuthProxy {
    suspend fun getToken(loginRequest: LoginRequest): LoginResponse
}