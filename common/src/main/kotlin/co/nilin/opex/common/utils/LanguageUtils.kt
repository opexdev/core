package co.nilin.opex.common.utils

import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.service.GlobalWebConfigCache
import reactor.core.publisher.Mono

object LanguageUtils {

    fun getUserLanguage(): Mono<String> =
        Mono.deferContextual { ctx -> Mono.just(ctx.getOrDefault("lang", getDefaultUserLanguage())!!) }

    fun getDefaultUserLanguage(): String {
       return try {
            GlobalWebConfigCache.webConfig?.defaultLanguage?.toString() ?: UserLanguage.EN.toString()
        } catch (e: Exception) {
            UserLanguage.EN.toString()
        }
    }
}