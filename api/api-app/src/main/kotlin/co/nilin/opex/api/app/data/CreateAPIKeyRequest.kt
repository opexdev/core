package co.nilin.opex.api.app.data

data class CreateApiKeyRequest(
    val apiKeyId: String?,
    val label: String?,
    val allowedIps: Set<String>?,
    val allowedEndpoints: Set<String>?,
    val keycloakUsername: String?
)