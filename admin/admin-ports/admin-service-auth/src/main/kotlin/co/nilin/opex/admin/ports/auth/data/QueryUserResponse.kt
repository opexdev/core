package co.nilin.opex.admin.ports.auth.data

data class QueryUserResponse(
    val total: Int,
    val users: List<KeycloakUser>
)