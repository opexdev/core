package co.nilin.opex.auth.service

import co.nilin.opex.auth.model.ChangePasswordTokenData
import co.nilin.opex.auth.model.OTPAction
import co.nilin.opex.auth.model.OTPType
import co.nilin.opex.auth.model.TokenData
import co.nilin.opex.common.utils.LoggerDelegate
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service
class TempTokenService(
    private val privateKey: PrivateKey,
    private val publicKey: PublicKey,
) {
    private val logger by LoggerDelegate()

    private val changePasswordKey: SecretKey = SecretKeySpec(
        MessageDigest.getInstance("SHA-256")
            .apply { update(CHANGE_PASSWORD_KEY_LABEL.toByteArray()) }
            .digest(privateKey.encoded),
        "AES"
    )

    fun generateToken(userId: String, action: OTPAction): String {
        val issuedAt = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        val exp = Date.from(LocalDateTime.now().plusMinutes(2).atZone(ZoneId.systemDefault()).toInstant())
        return Jwts.builder()
            .issuer(ISSUER)
            .claim("userId", userId)
            .claim("action", action)
            .issuedAt(issuedAt)
            .expiration(exp)
            .signWith(privateKey)
            .compact()
    }

    fun verifyToken(token: String): TokenData {
        try {
            val claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .payload
            return TokenData(true, claims["userId"] as String, OTPAction.valueOf(claims["action"] as String))
        } catch (e: JwtException) {
            logger.error("Could not verify token", e)
            return TokenData(false, "", OTPAction.REGISTER)
        }
    }

    fun generateChangePasswordToken(userId: String, otpType: OTPType, newPassword: String): String {
        val issuedAt = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        val exp = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant())
        return Jwts.builder()
            .issuer(ISSUER)
            .claim("userId", userId)
            .claim("action", OTPAction.CHANGE_PASSWORD)
            .claim("otpType", otpType)
            .claim("newPassword", newPassword)
            .issuedAt(issuedAt)
            .expiration(exp)
            .encryptWith(changePasswordKey, Jwts.ENC.A256GCM)
            .compact()
    }

    fun verifyChangePasswordToken(token: String): ChangePasswordTokenData {
        try {
            val claims = Jwts.parser()
                .requireIssuer(ISSUER)
                .decryptWith(changePasswordKey)
                .build()
                .parseEncryptedClaims(token)
                .payload
            if (claims["action"] != OTPAction.CHANGE_PASSWORD.name)
                return ChangePasswordTokenData(false, "", OTPType.NONE, "")
            return ChangePasswordTokenData(
                true,
                claims["userId"] as String,
                OTPType.valueOf(claims["otpType"] as String),
                claims["newPassword"] as String
            )
        } catch (e: Exception) {
            logger.error("Could not verify change password token", e)
            return ChangePasswordTokenData(false, "", OTPType.NONE, "")
        }
    }

    private companion object {
        const val ISSUER = "opex-auth"
        const val CHANGE_PASSWORD_KEY_LABEL = "opex-auth:change-password-token"
    }
}
