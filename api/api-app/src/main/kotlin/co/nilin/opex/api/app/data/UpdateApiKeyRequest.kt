package co.nilin.opex.api.app.data



data class UpdateApiKeyRequest(
    val label: String?,
    val enabled: Boolean?,
    val allowedIps: Set<String>?,
    val allowedEndpoints: Set<String>?,
    val keycloakUsername: String?
)


