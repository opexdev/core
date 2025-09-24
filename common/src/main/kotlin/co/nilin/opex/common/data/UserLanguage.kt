package co.nilin.opex.common.data

//After adjusting versions in various modules it should be moved to common project

enum class UserLanguage {
    EN, FA;

    companion object {
        fun safeValueOf(lang: String?): UserLanguage {
            return try {
                if (lang.isNullOrBlank()) EN
                else valueOf(lang.uppercase())
            } catch (e: IllegalArgumentException) {
                EN
            }
        }
    }
}