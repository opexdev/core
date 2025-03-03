package co.nilin.opex.matching.gateway.ports.postgres.service

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class PairSettingInitializer(
    private val pairSettingRepository: PairSettingRepository,
    private val redisCacheHelper: RedisCacheHelper,
    @Value("\${app.symbols}")
    private val symbols: String
) {

    private val logger = LoggerFactory.getLogger(PairSettingInitializer::class.java)
    val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun initialize() {
        logger.info(
            """
        ================================================    
        Initialize Pair Settings
        ================================================
        """
        )
        scope.launch {
            try {
                symbols.split(",").forEach { pair ->
                    val existingPair = pairSettingRepository.findByPair(pair).awaitFirstOrNull()

                    val pairToCache = existingPair ?: pairSettingRepository.insert(pair, true).awaitFirstOrNull()
                    logger.info("Added Pair: $pair")

                    redisCacheHelper.put(pair, pairToCache, DynamicInterval(5, TimeUnit.MINUTES))

                    if (existingPair != null) {
                        logger.info("Pair already exists: $pair")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error initializing Pair Settings: ${e.message}")
                throw e
            }
        }
    }
}
