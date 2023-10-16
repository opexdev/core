package co.nilin.opex.config.core.spi

import co.nilin.opex.config.core.inout.UserWebConfig

interface UserConfigManager {

    fun saveNewUserWebConfig(uuid: String, theme: String, language: String, favPairs: Set<String>): UserWebConfig

    fun updateThemeConfig(uuid: String, theme: String): UserWebConfig

    fun updateLanguageConfig(uuid: String, language: String): UserWebConfig

    fun updateFavoritePairsConfig(uuid: String, pairs: Set<String>): UserWebConfig

    fun addFavoritePair(uuid: String, pairs: Set<String>): UserWebConfig

    fun removeFavoritePair(uuid: String, pairs: Set<String>): UserWebConfig

    fun getUserConfig(uuid: String): UserWebConfig

}