package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.common.data.WebConfig

interface ConfigProxy {
    suspend fun getWebConfig(): WebConfig
    suspend fun updateWebConfig(token: String, request: UpdateWebConfigRequest): WebConfig
    suspend fun getUserLevelConfig(): List<UserLevelConfig>
    suspend fun updateUserLevelConfig(token: String, userLevelConfig: UserLevelConfig): UserLevelConfig
    suspend fun deleteUserLevelConfig(token: String, userLevel: String, language: String)
    suspend fun getUserConfig(token: String): UserWebConfig
    suspend fun updateUserConfig(token: String, request: UpdateUserConfigRequest): UserWebConfig
    suspend fun getUserFavoritePair(token: String): Set<String>
    suspend fun addUserFavoritePair(token: String, pair: String): Set<String>
    suspend fun removeUserFavoritePair(token: String, pair: String): Set<String>

    // Page messages
    suspend fun getLocalizedPageMessages(page: UiPage? = null, language: String): List<LocalizedPageMessage>
    suspend fun getAllPageMessages(token: String): List<PageMessage>
    suspend fun getPageMessage(token: String, page: UiPage): PageMessage?
    suspend fun createOrUpdatePageMessage(token: String, request: UpdatePageMessageRequest): PageMessage
    suspend fun deletePageMessage(token: String, page: UiPage)

}
