package co.nilin.opex.profile.ports.postgres.utils

import com.google.gson.Gson
import reactor.core.publisher.Mono

fun <T> Any.convert(classOfT: Class<T>): T = Gson().fromJson(Gson().toJson(this), classOfT)

fun <T> Mono<Any>.convert(classOfT: Class<T>): Mono<T> = Mono.just(Gson().fromJson(Gson().toJson(this), classOfT))
