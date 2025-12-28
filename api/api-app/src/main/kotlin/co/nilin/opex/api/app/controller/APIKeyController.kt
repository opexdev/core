package co.nilin.opex.api.app.controller

import co.nilin.opex.api.app.data.ApiKeyResponse
import co.nilin.opex.api.app.data.CreateApiKeyRequest
import co.nilin.opex.api.app.data.UpdateApiKeyRequest
import co.nilin.opex.common.security.JwtUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.SecureRandom
import java.util.*

@RestController
@RequestMapping("/v1/api-key")
class APIKeyController(
    private val apiKeyService: co.nilin.opex.api.core.spi.APIKeyService
) {

    private val rng = SecureRandom()

    private fun generateSecretBase64(bytes: Int = 48): String {
        val b = ByteArray(bytes)
        rng.nextBytes(b)
        return Base64.getEncoder().encodeToString(b)
    }

    private fun canonicalTemplate(): String = "METHOD\nPATH\nQUERY\nBODY_SHA256\nTIMESTAMP_MS"

    private fun headersTemplate(apiKeyId: String): Map<String, String> = mapOf(
        "X-API-KEY" to apiKeyId,
        "X-API-SIGNATURE" to "Base64(HMAC-SHA256(secret, canonical))",
        "X-API-TIMESTAMP" to "<epoch_ms>",
        "X-API-BODY-SHA256" to "<hex_sha256_body> (optional)"
    )

    // Create a new API key. Caller must provide a user access token; we bind the key to that user. Returns one-time secret and usage hints.
    @PostMapping
    suspend fun create(
        @RequestHeader(name = "Authorization", required = false) authorization: String?,
        @RequestBody req: CreateApiKeyRequest
    ): ApiKeyResponse {
        require(!authorization.isNullOrBlank() && authorization.startsWith("Bearer ")) { "Authorization Bearer user token is required" }
        val userToken = authorization.substringAfter("Bearer ").trim()
        val (userId, preferredUsername) = parseJwtUser(userToken)

        val apiKeyId = req.apiKeyId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val secret = generateSecretBase64()
        val stored = apiKeyService.createApiKeyRecord(
            apiKeyId = apiKeyId,
            label = req.label,
            plaintextSecret = secret,
            allowedIps = req.allowedIps,
            allowedEndpoints = req.allowedEndpoints,
            keycloakUserId = userId,
            keycloakUsername = preferredUsername,
            enabled = true
        )
        return ApiKeyResponse(
            apiKeyId = apiKeyId,
            label = stored.record.label,
            enabled = stored.record.enabled,
            allowedIps = stored.record.allowedIps,
            allowedEndpoints = stored.record.allowedEndpoints,
            keycloakUsername = stored.record.keycloakUsername,
            secret = secret
        )
    }

    private fun parseJwtUser(token: String): Pair<String, String?> {
        // Decode JWT payload using common JwtUtils (no signature verification here).
        val payload = JwtUtils.decodePayload(token)
        val sub = payload["sub"] as? String
        val preferred = payload["username"] as? String
        require(!sub.isNullOrBlank()) { "JWT missing sub" }
        return Pair(sub!!, preferred)
    }

    // List all API keys (admin-only) — secret is not returned
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_admin')")
    suspend fun list(): List<ApiKeyResponse> = apiKeyService.listApiKeyRecords().stream().map {
        ApiKeyResponse(
            apiKeyId = it.apiKeyId,
            label = it.label,
            enabled = it.enabled,
            allowedIps = it.allowedIps,
            allowedEndpoints = it.allowedEndpoints,
            keycloakUsername = it.keycloakUsername,
            secret = null
        )
    }.toList()


    // Get one API key (admin-only) — secret is not returned
    @GetMapping("/{apiKeyId}")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    suspend fun get(@PathVariable apiKeyId: String): ApiKeyResponse {
        val it = apiKeyService.getApiKeyRecord(apiKeyId) ?: throw NoSuchElementException("API key not found: $apiKeyId")
        return ApiKeyResponse(
            apiKeyId = it.apiKeyId,
            label = it.label,
            enabled = it.enabled,
            allowedIps = it.allowedIps,
            allowedEndpoints = it.allowedEndpoints,
            keycloakUsername = it.keycloakUsername,
            secret = null
        )
    }

    // Rotate secret (admin-only). Returns new one-time secret
    @PostMapping("/{apiKeyId}/rotate")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    suspend fun rotate(@PathVariable apiKeyId: String): ApiKeyResponse {
        val newSecret = generateSecretBase64()
        val stored = apiKeyService.rotateApiKeySecret(apiKeyId, newSecret)
        return ApiKeyResponse(
            apiKeyId = stored.record.apiKeyId,
            label = stored.record.label,
            enabled = stored.record.enabled,
            allowedIps = stored.record.allowedIps,
            allowedEndpoints = stored.record.allowedEndpoints,
            keycloakUsername = stored.record.keycloakUserId,
            secret = newSecret
        )
    }

    // Update metadata or enable/disable (admin-only)
    @PutMapping("/{apiKeyId}")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    suspend fun update(@PathVariable apiKeyId: String, @RequestBody req: UpdateApiKeyRequest): ApiKeyResponse {
        val s = apiKeyService.updateApiKeyRecord(
            apiKeyId = apiKeyId,
            label = req.label,
            enabled = req.enabled,
            allowedIps = req.allowedIps,
            allowedEndpoints = req.allowedEndpoints,
            keycloakUsername = req.keycloakUsername
        )
        return ApiKeyResponse(
            apiKeyId = s.apiKeyId,
            label = s.label,
            enabled = s.enabled,
            allowedIps = s.allowedIps,
            allowedEndpoints = s.allowedEndpoints,
            keycloakUsername = s.keycloakUserId
        )
    }

    // Delete/revoke (admin-only)
    @DeleteMapping("/{apiKeyId}")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    suspend fun delete(@PathVariable apiKeyId: String) {
        apiKeyService.deleteApiKeyRecord(apiKeyId)
    }
}