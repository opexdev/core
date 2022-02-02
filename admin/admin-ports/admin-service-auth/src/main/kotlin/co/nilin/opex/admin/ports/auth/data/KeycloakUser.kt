package co.nilin.opex.admin.ports.auth.data

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class KeycloakUser(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val isEnabled: Boolean,
    val isEmailVerified: Boolean,
    val groups:List<String>?,
    val requiredActions: List<String>?,
    val attributes: Map<String, List<String>>?
)