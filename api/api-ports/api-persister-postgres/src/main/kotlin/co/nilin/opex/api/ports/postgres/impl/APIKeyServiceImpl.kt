package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.spi.APIKeyService
import co.nilin.opex.api.core.spi.ApiKeySecretCrypto
import co.nilin.opex.api.core.utils.toCsv
import co.nilin.opex.api.core.utils.toSet
import co.nilin.opex.api.ports.postgres.dao.ApiKeyRegistryRepository
import co.nilin.opex.api.ports.postgres.model.ApiKeyRegistryModel
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class APIKeyServiceImpl(
    private val apiKeySecretCrypto: ApiKeySecretCrypto,
    private val apiKeyRegistryRepository: ApiKeyRegistryRepository
) : APIKeyService {

    private fun toRecord(e: ApiKeyRegistryModel): APIKeyService.ApiKeyRecord =
        APIKeyService.ApiKeyRecord(
            apiKeyId = e.apiKeyId,
            label = e.label,
            enabled = e.enabled,
            allowedIps = e.allowedIps.toSet(),
            allowedEndpoints = e.allowedEndpoints.toSet(),
            keycloakUserId = e.keycloakUserId,
            keycloakUsername = e.keycloakUsername
        )

    override suspend fun createApiKeyRecord(
        apiKeyId: String,
        label: String?,
        plaintextSecret: String,
        allowedIps: Set<String>?,
        allowedEndpoints: Set<String>?,
        keycloakUserId: String?,
        keycloakUsername: String?,
        enabled: Boolean
    ): APIKeyService.ApiKeyCreateResult {
        require(apiKeyId.isNotBlank()) { "apiKeyId is blank" }
        val exists = apiKeyRegistryRepository.existsById(apiKeyId).awaitSingle()
        if (exists) error("API key already exists: $apiKeyId")
        val enc = apiKeySecretCrypto.encrypt(plaintextSecret)
        val now = LocalDateTime.now()
        val entry = ApiKeyRegistryModel(
            apiKeyId = apiKeyId,
            label = label,
            encryptedSecret = enc,
            enabled = enabled,
            allowedIps = allowedIps.toCsv(),
            allowedEndpoints = allowedEndpoints.toCsv(),
            keycloakUserId = keycloakUserId,
            keycloakUsername = keycloakUsername,
            createdAt = now,
            updatedAt = now
        )
        val saved = apiKeyRegistryRepository.save(entry).awaitSingle()
        return APIKeyService.ApiKeyCreateResult(
            secret = plaintextSecret,
            record = toRecord(saved)
        )
    }

    override suspend fun rotateApiKeySecret(apiKeyId: String, newPlaintextSecret: String): APIKeyService.ApiKeyCreateResult {
        val existing = apiKeyRegistryRepository.findById(apiKeyId).awaitSingle() ?: error("API key not found: $apiKeyId")
        val enc = apiKeySecretCrypto.encrypt(newPlaintextSecret)
        val updated = existing.copy(
            encryptedSecret = enc,
            updatedAt = LocalDateTime.now()
        )
        val saved = apiKeyRegistryRepository.save(updated).awaitSingle()
        return APIKeyService.ApiKeyCreateResult(
            secret = newPlaintextSecret,
            record = toRecord(saved)
        )
    }

    override suspend fun updateApiKeyRecord(
        apiKeyId: String,
        label: String?,
        enabled: Boolean?,
        allowedIps: Set<String>?,
        allowedEndpoints: Set<String>?,
        keycloakUsername: String?
    ): APIKeyService.ApiKeyRecord {
        val existing = apiKeyRegistryRepository.findById(apiKeyId).awaitSingle() ?: error("API key not found: $apiKeyId")
        val updated = existing.copy(
            label = label ?: existing.label,
            enabled = enabled ?: existing.enabled,
            allowedIps = allowedIps.toCsv(),
            allowedEndpoints = allowedEndpoints.toCsv(),
            keycloakUsername = keycloakUsername ?: existing.keycloakUsername,
            updatedAt = LocalDateTime.now()
        )
        val saved = apiKeyRegistryRepository.save(updated).awaitSingle()
        return toRecord(saved)
    }

    override suspend fun getApiKeyRecord(apiKeyId: String): APIKeyService.ApiKeyRecord? {
        val e = apiKeyRegistryRepository.findById(apiKeyId).awaitSingle() ?: return null
        return toRecord(e)
    }

    override suspend fun listApiKeyRecords(): List<APIKeyService.ApiKeyRecord> =
        apiKeyRegistryRepository.findAll().map { toRecord(it) }.collectList().awaitSingle().sortedBy { it.apiKeyId }

    override suspend fun deleteApiKeyRecord(apiKeyId: String) {
        val exists = apiKeyRegistryRepository.existsById(apiKeyId).awaitSingle()
        if (!exists) error("API key not found: $apiKeyId")
        apiKeyRegistryRepository.deleteById(apiKeyId).awaitSingle()
    }

    override suspend fun getApiKeyForVerification(apiKeyId: String): APIKeyService.ApiKeyVerification? {
        val e = apiKeyRegistryRepository.findById(apiKeyId).awaitSingle() ?: return null
        val secret = try { apiKeySecretCrypto.decrypt(e.encryptedSecret) } catch (_: Exception) { return null }
        return APIKeyService.ApiKeyVerification(
            apiKeyId = apiKeyId,
            secret = secret,
            enabled = e.enabled,
            allowedEndpoints = e.allowedEndpoints.toSet(),
            allowedIps = e.allowedIps.toSet(),
            keycloakUserId = e.keycloakUserId
        )
    }
}