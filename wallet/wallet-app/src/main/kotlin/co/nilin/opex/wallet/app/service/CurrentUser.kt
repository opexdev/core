package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.dto.CurrentUser
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component


@Component
class CurrentUserProvider {
    suspend fun getCurrentUser(): CurrentUser? {
        val authentication = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()?.authentication
        if (authentication !is JwtAuthenticationToken) return null

        val jwt: Jwt = authentication.token

        return CurrentUser(
            uuid = jwt.getClaimAsString("id"),
            firstName = jwt.getClaimAsString("first_name"),
            lastName = jwt.getClaimAsString("last_name"),
            fullName = listOfNotNull(
                jwt.getClaimAsString("first_name"),
                jwt.getClaimAsString("last_name")
            ).joinToString(" "),
            mobile = jwt.getClaimAsString("mobile"),
            roles = jwt.getClaimAsStringList("roles") ?: emptyList(),
            level = jwt.getClaimAsString("level")
        )
    }
}

