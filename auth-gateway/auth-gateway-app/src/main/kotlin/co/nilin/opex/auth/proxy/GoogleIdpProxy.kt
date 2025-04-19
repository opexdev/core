package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.KeycloakConfig
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import java.net.URL
import java.security.interfaces.RSAPublicKey

@Service
class GoogleProxy(private val keycloakConfig: KeycloakConfig) {

    fun validateGoogleToken(googleToken: String): DecodedJWT {
        // Step 1: Fetch Google's public keys
        val jwkProvider: JwkProvider = JwkProviderBuilder(URL("https://www.googleapis.com/oauth2/v3/certs"))
            .build()

        // Step 2: Decode and verify the token
        val algorithm = Algorithm.RSA256(jwkProvider.get(JWT.decode(googleToken).keyId).publicKey as RSAPublicKey, null)
        val verifier = JWT.require(algorithm)
            .withIssuer("https://accounts.google.com")
            .build()

        val decoded = verifier.verify(googleToken)
        if ( decoded.audience.isEmpty() || !decoded.audience.contains(keycloakConfig.googleClientId)){
           throw JWTVerificationException("Google token's audience doesn't match")
        }
        return decoded
    }
}