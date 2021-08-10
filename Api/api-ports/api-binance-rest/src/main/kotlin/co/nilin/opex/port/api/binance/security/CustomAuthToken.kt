package co.nilin.opex.port.api.binance.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CustomAuthToken(
    val uuid: String?,
    val fullName: String?,
    val username: String?,
    val email: String?,
    val tokenValue: String?,
    authorities: Collection<GrantedAuthority> = arrayListOf()
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any {
        return "N/A"
    }

    override fun getPrincipal(): Any? {
        return uuid
    }
}