package co.nilin.opex.api.core.spi

interface ApiKeySecretCrypto {
    fun encrypt(plaintext: String): String
    fun decrypt(ciphertext: String): String
}