package co.nilin.opex.auth.config

import co.nilin.opex.auth.kafka.KycLevelUpdatedKafkaListener
import co.nilin.opex.auth.kafka.ProfileUpdatedKafkaListener
import co.nilin.opex.auth.spi.KycLevelUpdatedEventListener
import co.nilin.opex.auth.spi.ProfileUpdatedEventListener
import jakarta.annotation.PostConstruct
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Configuration
class AppConfig {

    @PostConstruct
    fun init() {
        val pemFile = File("/app/keys/private.pem")
        if (pemFile.exists())
            return

        val keypair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val privateKeyPem = convertPrivateKeyToPem(keypair.private)

        File("/app/keys").apply { if (!exists()) mkdir() }
        OutputStreamWriter(FileOutputStream("/app/keys/private.pem")).use { it.write(privateKeyPem) }
    }

    @Bean("privateKeyString")
    fun privateKeyString(): String {
        return File("/app/keys/private.pem").readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
    }

    @Bean("privateKey")
    fun privateKey(): PrivateKey {
        val pKeyString = privateKeyString()
        val keyBytes = Base64.getDecoder().decode(pKeyString)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    @Bean("publicKey")
    fun publicKey(): PublicKey {
        val privateKey = privateKey() as RSAPrivateCrtKey
        val publicKeySpec = RSAPublicKeySpec(privateKey.modulus, privateKey.publicExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(publicKeySpec)
    }

    private fun convertPrivateKeyToPem(privateKey: PrivateKey): String {
        val keySpec = PKCS8EncodedKeySpec(privateKey.encoded)
        val pemObject = PemObject("PRIVATE KEY", keySpec.encoded)
        val stringWriter = StringWriter()
        PemWriter(stringWriter).use { it.writeObject(pemObject) }
        return stringWriter.toString()
    }

    @Autowired
    fun configureEventListeners(
        kycLevelUpdatedKafkaListener: KycLevelUpdatedKafkaListener,
        kycLevelUpdatedEventListener: KycLevelUpdatedEventListener,
        profileUpdatedKafkaListener: ProfileUpdatedKafkaListener,
        profileUpdatedEventListener: ProfileUpdatedEventListener,
    ) {
        kycLevelUpdatedKafkaListener.addEventListener(kycLevelUpdatedEventListener)
        profileUpdatedKafkaListener.addEventListener(profileUpdatedEventListener)
    }
}