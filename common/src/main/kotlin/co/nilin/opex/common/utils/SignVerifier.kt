package co.nilin.opex.common.utils


import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class SignVerifier{

    fun verify(signatureAlgorithm: String , publicKeyPem: String, payload: String, signatureBase64: String) {
        val signatureBytes = try {
            Base64.getDecoder().decode(signatureBase64)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid fiat scanner signature encoding")
        }

        val publicKey = try {
            val normalizedPem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")

            val keyBytes = Base64.getDecoder().decode(normalizedPem)
            val keySpec = X509EncodedKeySpec(keyBytes)

            KeyFactory
                .getInstance(resolveKeyFactoryAlgorithm(signatureAlgorithm))
                .generatePublic(keySpec)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid fiat scanner public key")
        }

        val verified = try {
            Signature.getInstance(signatureAlgorithm).apply {
                initVerify(publicKey)
                update(payload.toByteArray(Charsets.UTF_8))
            }.verify(signatureBytes)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fiat scanner signature verification failed")
        }

        if (!verified) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid fiat scanner signature")
        }
    }

    private fun resolveKeyFactoryAlgorithm(algorithm: String): String {
        return when {
            algorithm.equals("Ed25519", ignoreCase = true) -> "Ed25519"
            algorithm.contains("RSA", ignoreCase = true) -> "RSA"
            algorithm.contains("ECDSA", ignoreCase = true) -> "EC"
            else -> "RSA"
        }
    }
}