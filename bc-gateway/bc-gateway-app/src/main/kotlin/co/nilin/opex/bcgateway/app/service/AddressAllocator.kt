package co.nilin.opex.bcgateway.app.service

import co.nilin.opex.bcgateway.core.spi.AddressManager
import co.nilin.opex.bcgateway.core.utils.LoggerDelegate
import org.slf4j.Logger
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit
@Configuration
class AddressAllocator (private val addressManager: AddressManager){
    private val logger: Logger by LoggerDelegate()

    @Scheduled(fixedRate = 10 , timeUnit = TimeUnit.SECONDS)
    fun revokeExpiredAddress(){
        logger.info("going to lookup assigned address .....")
        addressManager.revokeExpiredAddress()
    }
}