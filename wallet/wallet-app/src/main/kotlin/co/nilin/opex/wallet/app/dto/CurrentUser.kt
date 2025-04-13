package co.nilin.opex.wallet.app.dto

data class CurrentUser(
        val uuid: String?,
        val firstName: String?,
        val lastName: String?,
        val fullName: String?,
        val mobile: String?,
        val roles: List<String>,
        val level: String?
)