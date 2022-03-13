package co.nilin.opex.auth.gateway.data

data class ChangePasswordRequest(
    val password: String,
    val newPassword: String,
    val confirmation: String,
)