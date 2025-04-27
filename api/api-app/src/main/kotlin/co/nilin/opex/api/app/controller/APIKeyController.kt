package co.nilin.opex.api.app.controller

import co.nilin.opex.api.app.data.APIKeyResponse
import co.nilin.opex.api.app.data.CreateAPIKeyRequest
import co.nilin.opex.api.app.service.APIKeyServiceImpl
import co.nilin.opex.api.ports.binance.util.jwtAuthentication
import co.nilin.opex.api.ports.binance.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/api-key")
class APIKeyController(private val apiKeyService: APIKeyServiceImpl) {

    @GetMapping
    suspend fun getKeys(principal: Principal): List<APIKeyResponse> {
        return apiKeyService.getKeysByUserId(principal.name)
            .map { APIKeyResponse(it.label, it.expirationTime, it.allowedIPs, it.key, it.isEnabled) }
    }

    @PostMapping
    suspend fun create(
        @RequestBody request: CreateAPIKeyRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Any {
        val jwt = securityContext.jwtAuthentication()
        val response = apiKeyService.createAPIKey(
            jwt.name,
            request.label,
            request.expiration?.getLocalDateTime(),
            request.allowedIPs,
            jwt.tokenValue()
        )
        return object {
            val apiKey = response.second.key
            val secret = response.first
        }
    }

    @PutMapping("/{key}/enable")
    suspend fun enableKey(principal: Principal, @PathVariable key: String) {
        apiKeyService.changeKeyState(principal.name, key, true)
    }

    @PutMapping("/{key}/disable")
    suspend fun disableKey(principal: Principal, @PathVariable key: String) {
        apiKeyService.changeKeyState(principal.name, key, false)
    }

    @DeleteMapping("/{key}")
    suspend fun deleteKey(principal: Principal, @PathVariable key: String) {
        apiKeyService.deleteKey(principal.name, key)
    }

}