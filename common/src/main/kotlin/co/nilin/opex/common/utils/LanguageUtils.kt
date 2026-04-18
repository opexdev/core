package co.nilin.opex.common.utils

import co.nilin.opex.common.data.UserLanguage
import reactor.core.publisher.Mono

object LanguageUtils {

    fun getUserLanguage(): Mono<String> =
        Mono.deferContextual { ctx -> Mono.just(ctx.getOrDefault("lang", UserLanguage.EN.toString())!!) }

    fun getDefaultUserLanguage(): String = UserLanguage.EN.toString()
}