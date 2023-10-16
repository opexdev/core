package co.nilin.opex.config.core.inout

data class UserWebConfig(
    val theme: String,
    val language: String,
    val favoritePairs: Set<String> = hashSetOf()
)