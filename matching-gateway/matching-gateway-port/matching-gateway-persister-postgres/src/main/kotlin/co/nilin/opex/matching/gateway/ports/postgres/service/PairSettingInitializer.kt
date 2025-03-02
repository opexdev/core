package co.nilin.opex.matching.gateway.ports.postgres.service

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.model.PairSettingModel
import co.nilin.opex.matching.gateway.ports.postgres.util.RedisCacheHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class PairSettingInitializer(
    private val pairSettingRepository: PairSettingRepository,
    private val redisCacheHelper: RedisCacheHelper
) {
    private val logger = LoggerFactory.getLogger(PairSettingInitializer::class.java)
    private val defaultPairs = System.getenv("SYMBOLS").split(",")
    val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    @Transactional
    fun initialize() {
        logger.info(
            """
        ================================================    
        Initialize ${defaultPairs.count()} Pair Settings
        ================================================
        """
        )
        scope.launch {
            try {
                defaultPairs.forEach { pair ->
                    val existingPair = pairSettingRepository.findByPair(pair).awaitFirstOrNull()
                    val pairToCache = existingPair ?: PairSettingModel(pair).also { newPair ->
                        pairSettingRepository.save(newPair)
                        logger.info("Added Pair: $pair")
                    }
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
