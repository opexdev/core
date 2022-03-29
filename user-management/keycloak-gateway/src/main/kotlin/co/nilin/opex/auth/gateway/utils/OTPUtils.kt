package co.nilin.opex.auth.gateway.utils

import org.keycloak.models.RealmModel
import org.keycloak.models.credential.OTPCredentialModel
import org.keycloak.models.utils.Base32
import java.net.URLEncoder

object OTPUtils {

    fun generateOTPKeyURI(
        realm: RealmModel,
        secret: String,
        displayName: String,
        accountName: String
    ): String {
        val policy = realm.otpPolicy
        val accountNameEncode = URLEncoder.encode(accountName, Charsets.UTF_8)
        val issuer = URLEncoder.encode(displayName, Charsets.UTF_8)
        val label = "$issuer:$accountNameEncode".replace("\\+", "%20")

        val params = StringBuilder()
            .append("secret=${Base32.encode(secret.toByteArray())}")
            .append("&digits=${policy.digits}")
            .append("&algorithm=SHA1")
            .append("&issuer=$issuer")
            .append(
                if (policy.type == OTPCredentialModel.HOTP)
                    "&counter=${policy.initialCounter}"
                else
                    "&period=${policy.period}"
            )

        return "otpauth://${policy.type}/${label}?${params}"
    }

}