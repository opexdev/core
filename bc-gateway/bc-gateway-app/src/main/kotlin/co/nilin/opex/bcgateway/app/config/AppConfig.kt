package co.nilin.opex.bcgateway.app.config


import co.nilin.opex.bcgateway.core.api.AssignAddressService
import co.nilin.opex.bcgateway.core.api.InfoService
import co.nilin.opex.bcgateway.core.service.AssignAddressServiceImpl
import co.nilin.opex.bcgateway.core.service.InfoServiceImpl
import co.nilin.opex.bcgateway.core.spi.AssignedAddressHandler
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.ReservedAddressHandler
import co.nilin.opex.bcgateway.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.bcgateway.ports.kafka.listener.spi.AdminEventListener
import co.nilin.opex.bcgateway.ports.postgres.impl.CurrencyHandlerImplV2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


@Configuration
class AppConfig(private val resourceLoader: ResourceLoader) {

    @Bean
    fun assignAddressService(
        currencyHandler: CurrencyHandlerImplV2,
        assignedAddressHandler: AssignedAddressHandler,
        reservedAddressHandler: ReservedAddressHandler,
        chainLoader: ChainLoader
    ): AssignAddressService {
        return AssignAddressServiceImpl(currencyHandler, assignedAddressHandler, reservedAddressHandler, chainLoader)
    }

    @Bean
    fun infoService(): InfoService {
        return InfoServiceImpl()
    }

    @Autowired
    fun configureEventListeners(
        adminKafkaEventListener: AdminEventKafkaListener,
        adminEventListener: AdminEventListener,
    ) {
        adminKafkaEventListener.addEventListener(adminEventListener)
    }

    @Bean("webhookPublicKey")
    fun webhookPublicKey(): PublicKey {
        val publicKeyString = resourceLoader.getResource("classpath:scanner-public.pem").inputStream
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")

        val keyBytes = Base64.getDecoder().decode(publicKeyString)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

}
