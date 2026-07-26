package co.nilin.opex.wallet.ports.postgres.service

import co.nilin.opex.wallet.core.spi.CurrencyServiceManager
import co.nilin.opex.wallet.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class CurrencyCacheInitializer(
    private val redisCacheHelper: RedisCacheHelper, private val currencyServiceManager: CurrencyServiceManager
) {
    private val logger = LoggerFactory.getLogger(CurrencyCacheInitializer::class.java)
    val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun initialize() {
        logger.info(
            """
================================================================================================
                                Initialize Currency Cache
================================================================================================
        """
        )
        scope.launch {
            try {
                currencyServiceManager.fetchAllCurrenciesPrecision().forEach { currency ->
                    redisCacheHelper.put(
                        "${currency.symbol}-precision", currency.precision
                    )
                    logger.info("${currency.symbol} : ${currency.precision}")
                }
                logger.info(
                    """
================================================================================================
                                 Completed Successfully
================================================================================================
            """
                )

            } catch (e: Exception) {
                logger.error("Error initializing Currency Cache: ${e.message}")
                throw e
            }


        }
    }
}