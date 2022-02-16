package co.nilin.opex.admin.ports.auth.utils

import co.nilin.opex.admin.ports.auth.data.KeycloakUser
import org.keycloak.representations.idm.UserRepresentation

fun UserRepresentation.asKeycloakUser(): KeycloakUser = KeycloakUser(
    id,
    email,
    username,
    firstName,
    lastName,
    isEnabled,
    isEmailVerified,
    groups,
    requiredActions,
    attributes
)