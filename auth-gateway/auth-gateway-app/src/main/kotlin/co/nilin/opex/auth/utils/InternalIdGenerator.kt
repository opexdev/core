package co.nilin.opex.auth.utils

fun generateRandomID(length: Int = 8): String {
    val charset = ('0'..'9') + ('a'..'z')
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}

