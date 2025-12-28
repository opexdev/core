package co.nilin.opex.api.core.spi

interface APIKeyService {

    data class ApiKeyRecord(
        val apiKeyId: String,
        val label: String?,
        val enabled: Boolean,
        val allowedIps: Set<String>?,
        val allowedEndpoints: Set<String>?,
        val keycloakUserId: String?,
        val keycloakUsername: String?
    )

    data class ApiKeyCreateResult(
        val secret: String,
        val record: ApiKeyRecord
    )

    data class ApiKeyVerification(
        val apiKeyId: String,
        val secret: String,
        val enabled: Boolean,
        val allowedEndpoints: Set<String>?,
        val allowedIps: Set<String>?,
        val keycloakUserId: String?
    )

    suspend fun createApiKeyRecord(
        apiKeyId: String,
        label: String?,
        plaintextSecret: String,
        allowedIps: Set<String>?,
        allowedEndpoints: Set<String>?,
        keycloakUserId: String?,
        keycloakUsername: String?,
        enabled: Boolean
    ): ApiKeyCreateResult

    suspend fun rotateApiKeySecret(apiKeyId: String, newPlaintextSecret: String): ApiKeyCreateResult

    suspend fun updateApiKeyRecord(
        apiKeyId: String,
        label: String?,
        enabled: Boolean?,
        allowedIps: Set<String>?,
        allowedEndpoints: Set<String>?,
        keycloakUsername: String?
    ): ApiKeyRecord

    suspend fun getApiKeyRecord(apiKeyId: String): ApiKeyRecord?

    suspend fun listApiKeyRecords(): List<ApiKeyRecord>

    suspend fun deleteApiKeyRecord(apiKeyId: String)

    suspend fun getApiKeyForVerification(apiKeyId: String): ApiKeyVerification?
}