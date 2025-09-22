package co.nilin.opex.wallet.core.model

enum class UserRole(
    val priority: Int,
    val keycloakName: String
) {
    SUPER_ADMIN(5, "super-admin"),
    ADMIN(4, "admin"),
    USER_3(3, "user-3"),
    USER_2(2, "user-2"),
    USER_1(1, "user-1");

    companion object {
        fun getHighestRoleKeycloakName(roles: List<String>): String? {
            return roles.mapNotNull { roleName ->
                values().find { it.keycloakName == roleName }
            }.maxByOrNull { it.priority }?.keycloakName
        }
    }
}


