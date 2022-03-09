package co.nilin.opex.auth.gateway.data

data class KycRequest(
    val selfiePath: String,
    val idCardPath: String,
    val acceptFormPath: String
)