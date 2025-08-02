package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.APIKey
import java.time.LocalDateTime

interface APIKeyService {

    fun createAPIKey(
        userId: String,
        label: String,
        expirationTime: LocalDateTime?,
        allowedIPs: String?,
        currentToken: String
    ): Pair<String, APIKey>

    fun getAPIKey(key: String, secret: String): APIKey?

    fun getKeysByUserId(userId: String): List<APIKey>

    fun changeKeyState(userId: String, key: String, isEnabled: Boolean)

    fun deleteKey(userId: String, key: String)

}