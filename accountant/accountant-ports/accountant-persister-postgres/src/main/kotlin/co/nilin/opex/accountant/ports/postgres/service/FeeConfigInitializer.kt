package co.nilin.opex.accountant.ports.postgres.service

import co.nilin.opex.accountant.ports.postgres.util.RedisCacheHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.annotation.PostConstruct


@Service
class FeeConfigInitializer(private val redisCacheHelper: RedisCacheHelper) {

    private val logger = LoggerFactory.getLogger(FeeConfigInitializer::class.java)

    data class Fee(val maker: BigDecimal, val taker: BigDecimal)
    private val initialFees = mapOf(
        "user-1" to Fee(BigDecimal("0.5"), BigDecimal("0.6")),
        "user-2" to Fee(BigDecimal("0.3"), BigDecimal("0.35")),
        "user-3" to Fee(BigDecimal("0.2"), BigDecimal("0.25"))
    )

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
            initialFees.forEach { (user, fee) ->
                putAndLog("fee:maker:$user", fee.maker)
                putAndLog("fee:taker:$user", fee.taker)
            }

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