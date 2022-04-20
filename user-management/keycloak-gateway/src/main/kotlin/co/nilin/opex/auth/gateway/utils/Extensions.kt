package co.nilin.opex.auth.gateway.utils

fun <T> tryOrElse(alt: T?, body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {
        alt
    }
}