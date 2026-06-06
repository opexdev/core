package co.nilin.opex.common.data

import co.nilin.opex.common.service.GlobalWebConfigCache

enum class UserLanguage {
    EN, FA, AR, UZ;

    companion object {

        fun getDefaultLanguage(): String =
            GlobalWebConfigCache.webConfig?.defaultLanguage?.name ?: EN.name

        fun safeValueOf(lang: String?): UserLanguage {
            return try {
                if (lang.isNullOrBlank()) valueOf(getDefaultLanguage())
                else valueOf(lang.uppercase())
            } catch (e: IllegalArgumentException) {
                valueOf(getDefaultLanguage())
            }
        }
    }
}