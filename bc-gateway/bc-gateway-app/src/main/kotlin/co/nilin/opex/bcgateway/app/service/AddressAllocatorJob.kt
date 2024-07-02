package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.core.spi.AddressManager
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit

@Configuration
class AddressAllocatorJob(private val addressManager: AddressManager) {
    private val logger: Logger by LoggerDelegate()

    @Value("\${app.address.life-time}")
    private var addressLifeTime : Long? = null

    @Scheduled(fixedDelayString = "60000")
    fun revokeExpiredAddress() {
        if (addressLifeTime != null) {
            logger.info("going to lookup assigned address .....")
            runBlocking { addressManager.revokeExpiredAddress() }
        }
    }
}