package co.nilin.opex.bcgateway.app.utils

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(
    private val allowedAudiences: Set<String>
) : OAuth2TokenValidator<Jwt> {

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val tokenAudiences = jwt.audience

        val matched = tokenAudiences.any() { it in allowedAudiences }

        return if (matched) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(
                OAuth2Error(
                    "invalid_token",
                    "Invalid audience",
                    null
                )
            )
        }
    }
}