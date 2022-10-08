package co.nilin.opex.auth.gateway.data

data class Whitelist(
    val isEnabled: Boolean = false,
    val emails: List<String> = emptyList()
)