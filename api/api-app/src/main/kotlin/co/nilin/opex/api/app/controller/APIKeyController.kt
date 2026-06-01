package co.nilin.opex.api.app.controller

import co.nilin.opex.api.app.data.ApiKeyResponse
import co.nilin.opex.api.app.data.CreateApiKeyRequest
import co.nilin.opex.api.app.data.UpdateApiKeyRequest
import co.nilin.opex.common.security.JwtUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.security.SecureRandom
import java.util.*

@RestController
@RequestMapping("/v1/api-key")
@Tag(
    name = "API App - API Keys",
    description = "API key management operations."
)
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

    @PostMapping
    @Operation(
        summary = "Create API key",
        description = """POST /v1/api-key.
Security: Bearer user-token required.

Behavior: Creates a new API key for the authenticated user. The generated secret is returned only once in this response.
Source of values: Use the Bearer token of the user who should own the API key.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiKeyResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun create(
        @Parameter(hidden = true)
        @RequestHeader(name = "Authorization", required = false)
        authorization: String?,
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
        val payload = JwtUtils.decodePayload(token)
        val sub = payload["sub"] as? String
        val preferred = payload["username"] as? String
        require(!sub.isNullOrBlank()) { "JWT missing sub" }
        return Pair(sub!!, preferred)
    }

    @GetMapping
    @Operation(
        summary = "List API keys",
        description = """GET /v1/api-key.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Returns API key metadata. Secrets are not returned.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ApiKeyResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
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

    @GetMapping("/{apiKeyId}")
    @Operation(
        summary = "Get API key",
        description = """GET /v1/api-key/{apiKeyId}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Returns API key metadata. Secret is not returned.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [Parameter(
            name = "apiKeyId",
            `in` = ParameterIn.PATH,
            required = true,
            description = "API key id."
        )],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiKeyResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
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

    @PostMapping("/{apiKeyId}/rotate")
    @Operation(
        summary = "Rotate API key secret",
        description = """POST /v1/api-key/{apiKeyId}/rotate.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Rotates the API key secret. The new secret is returned only once in this response.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [Parameter(
            name = "apiKeyId",
            `in` = ParameterIn.PATH,
            required = true,
            description = "API key id."
        )],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiKeyResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
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

    @PutMapping("/{apiKeyId}")
    @Operation(
        summary = "Update API key",
        description = """PUT /v1/api-key/{apiKeyId}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Updates API key metadata and enabled status. Secret is not returned.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [Parameter(
            name = "apiKeyId",
            `in` = ParameterIn.PATH,
            required = true,
            description = "API key id."
        )],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ApiKeyResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
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

    @DeleteMapping("/{apiKeyId}")
    @Operation(
        summary = "Delete API key",
        description = """DELETE /v1/api-key/{apiKeyId}.
Security: Bearer admin-token required. Required authority: ROLE_admin.

Behavior: Deletes or revokes the API key.
Response body: No response body.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [Parameter(
            name = "apiKeyId",
            `in` = ParameterIn.PATH,
            required = true,
            description = "API key id."
        )],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun delete(@PathVariable apiKeyId: String) {
        apiKeyService.deleteApiKeyRecord(apiKeyId)
    }
}
