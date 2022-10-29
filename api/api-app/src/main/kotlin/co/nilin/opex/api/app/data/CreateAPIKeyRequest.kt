package co.nilin.opex.api.app.data

data class CreateAPIKeyRequest(
    val label: String,
    val expiration: APIKeyExpiration?,
    val allowedIPs: String?
)