package co.nilin.opex.config.app.dto

data class UpdateUserConfigRequest(
    val theme: String?,
    val language: String?,
    val favoritePairs: Set<String>?
)