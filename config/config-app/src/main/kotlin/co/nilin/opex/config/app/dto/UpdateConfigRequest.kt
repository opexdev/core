package co.nilin.opex.config.app.dto

data class UpdateConfigRequest(
    val theme: String?,
    val language: String?,
    val favoritePairs: Set<String>?
)