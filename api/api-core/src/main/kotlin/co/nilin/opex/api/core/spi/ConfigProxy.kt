package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.UpdateUserConfigRequest
import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.api.core.inout.UserLevelConfig
import co.nilin.opex.api.core.inout.UserWebConfig
import co.nilin.opex.common.data.WebConfig

interface ConfigProxy {
    suspend fun getWebConfig(): WebConfig
    suspend fun updateWebConfig(token: String, request: UpdateWebConfigRequest): WebConfig
    suspend fun getUserLevelConfig(): List<UserLevelConfig>
    suspend fun updateUserLevelConfig(token: String, userLevelConfig: UserLevelConfig): UserLevelConfig
    suspend fun deleteUserLevelConfig(token: String, userLevel: String, language: String)
    suspend fun getUserConfig(token: String): UserWebConfig
    suspend fun updateUserConfig(token: String, request: UpdateUserConfigRequest): UserWebConfig
    suspend fun addUserFavoritePair(token: String, pairs: Set<String>): UserWebConfig
    suspend fun removeUserFavoritePair(token: String, pairs: Set<String>): UserWebConfig

}
