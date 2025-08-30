package co.nilin.opex.common.utils

import co.nilin.opex.utility.error.data.UserLanguage
import co.nilin.opex.utility.interceptors.UserLanguageResolver

fun justTry(action: () -> Unit) {
    try {
        action()
    } catch (_: Exception) {
    }
}

fun userLanguageResolver(): UserLanguage? {
   return UserLanguage.safeValueOf(UserLanguageResolver.resolveUserLanguage())

}