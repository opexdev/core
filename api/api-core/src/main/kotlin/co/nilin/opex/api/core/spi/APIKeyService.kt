package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.APIKey
import java.time.LocalDateTime

interface APIKeyService {

    suspend fun createAPIKey(
        userId: String,
        label: String,
        expirationTime: LocalDateTime?,
        allowedIPs: String?,
        currentToken: String
    ): Pair<String, APIKey>

    suspend fun getAPIKey(key: String, secret: String): APIKey?

    suspend fun getKeysByUserId(userId: String): List<APIKey>

    suspend fun changeKeyState(userId: String, key: String, isEnabled: Boolean)

    suspend fun deleteKey(userId: String, key: String)

}