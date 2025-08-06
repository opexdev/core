package co.nilin.opex.accountant.ports.postgres.service

import co.nilin.opex.accountant.ports.postgres.util.RedisCacheHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Service
class FeeConfigInitializer(private val redisCacheHelper: RedisCacheHelper) {
    private val logger = LoggerFactory.getLogger(FeeConfigInitializer::class.java)

    @PostConstruct
    fun initialize() {
        logger.info(
            """
================================================================================
                             Starting Fee Configuration Initialization
================================================================================
        """.trimIndent()
        )
        try {
            putAndLog("fee:maker:user-1", BigDecimal.valueOf(0.5))
            putAndLog("fee:taker:user-1", BigDecimal.valueOf(0.5))
            putAndLog("fee:maker:user-2", BigDecimal.valueOf(0.3))
            putAndLog("fee:taker:user-2", BigDecimal.valueOf(0.3))
            putAndLog("fee:maker:user-3", BigDecimal.valueOf(0.2))
            putAndLog("fee:taker:user-3", BigDecimal.valueOf(0.2))

            logger.info(
                """
================================================================================
                     Fee Configuration Initialization Completed Successfully
================================================================================
            """.trimIndent()
            )
        } catch (e: Exception) {
            logger.error(
                """
================================================================================
                   Error During Fee Configuration Initialization: ${e.message}
================================================================================
            """.trimIndent(), e
            )
            throw e
        }
    }

    private fun putAndLog(key: String, value: BigDecimal) {
        redisCacheHelper.put(key, value)
        logger.info("Set fee config -> key: '{}', value: '{}'", key, value)
    }
}