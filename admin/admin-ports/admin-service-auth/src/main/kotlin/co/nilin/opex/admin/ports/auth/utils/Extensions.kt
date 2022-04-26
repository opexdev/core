package co.nilin.opex.admin.ports.auth.utils

import co.nilin.opex.admin.ports.auth.data.KeycloakUser
import org.keycloak.representations.idm.UserRepresentation

fun UserRepresentation.asKeycloakUser(includeAttributes: Boolean = false): KeycloakUser = KeycloakUser(
    id,
    email,
    username,
    firstName,
    lastName,
    isEnabled,
    isEmailVerified,
    requiredActions,
    if(includeAttributes) attributes else null
)