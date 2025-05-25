package co.nilin.opex.matching.gateway.ports.postgres.service

import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.util.CacheManager
import co.nilin.opex.matching.gateway.ports.postgres.util.toPairSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class PairSettingInitializer(
    private val pairSettingRepository: PairSettingRepository,
    private val cacheManager: CacheManager<String, PairSetting>,
    @Value("\${app.symbols}")
    private val symbols: String
) {

    private val logger = LoggerFactory.getLogger(PairSettingInitializer::class.java)
    val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun initialize() {
        logger.info(
            """
================================================================================================
                                Initialize Pair Settings
================================================================================================
        """
        )
        scope.launch {
            try {
                symbols.split(",").forEach { pair ->
                    val existingPair = pairSettingRepository.findByPair(pair).awaitFirstOrNull()

                    val pairToCache = existingPair ?: pairSettingRepository.insert(
                        pair,
                        false,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        "LIMIT_ORDER,MARKET_ORDER"
                    ).then(pairSettingRepository.findByPair(pair)).awaitFirstOrNull()
                        .also { if (it == null) logger.warn("Failed to insert pair: $pair") }
                    ?: return@forEach

                    if (existingPair != null) logger.info("Pair already exists: $pair") else logger.info("Added Pair: $pair")

                    cacheManager.put(pair, pairToCache.toPairSetting(), 5, TimeUnit.MINUTES)
                }
                logger.info(
                    """
================================================================================================
                                 Completed Successfully
================================================================================================
            """
                )
            } catch (e: Exception) {
                logger.error("Error initializing Pair Settings: ${e.message}")
                throw e
            }
        }
    }
}
