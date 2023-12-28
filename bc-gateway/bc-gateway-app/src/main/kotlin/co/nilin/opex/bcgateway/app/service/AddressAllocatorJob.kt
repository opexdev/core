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

    @Value("\${app.address.life-time.value}")
    private var lifeTime: Long? = null

    @Value("\${app.address.life-time.unit}")
    private var lifeUnit: String? = "minute"

    //todo flexibleUnitTime
    @Scheduled(fixedDelayString = "\${app.address.life-time.value:0}000")
    fun revokeExpiredAddress() {
        if (lifeTime != null) {
            logger.info("going to lookup assigned address .....")
            runBlocking { addressManager.revokeExpiredAddress() }
        }
    }
}