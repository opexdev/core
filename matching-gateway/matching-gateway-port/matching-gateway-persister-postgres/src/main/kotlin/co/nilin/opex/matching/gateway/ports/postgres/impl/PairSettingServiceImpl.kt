package co.nilin.opex.matching.gateway.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.gateway.ports.postgres.dao.PairSettingRepository
import co.nilin.opex.matching.gateway.ports.postgres.dto.PairSetting
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import co.nilin.opex.matching.gateway.ports.postgres.util.CacheManager
import co.nilin.opex.matching.gateway.ports.postgres.util.toPairSetting
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class PairSettingServiceImpl(
    private val pairSettingRepository: PairSettingRepository,
    private val cacheManager: CacheManager<String, PairSetting>
) : PairSettingService {

    override suspend fun load(pair: String): PairSetting {
        return cacheManager.get("pair-setting:$pair")
            ?: pairSettingRepository.findByPair(pair)
                .awaitFirstOrNull()
                ?.let {
                    it.toPairSetting().also {
                        cacheManager.put(
                            "pair-setting:${it.pair}",
                            it,
                            5, TimeUnit.MINUTES
                        )
                    }
                }
            ?: throw OpexError.PairNotFound.exception()
    }

    override suspend fun loadAll(): List<PairSetting> {
        return pairSettingRepository.findAll()
            .map { it.toPairSetting() }
            .collectList()
            .awaitFirstOrNull() ?: emptyList()
    }

    override suspend fun update(pairSetting: PairSetting): PairSetting {
        val pairSetting =
            pairSettingRepository.findByPair(pairSetting.pair).awaitFirstOrNull() ?: throw OpexError.PairNotFound.exception()
        pairSetting.apply {
            this.isAvailable = pairSetting.isAvailable
            this.minOrder = pairSetting.minOrder
            this.maxOrder = pairSetting.maxOrder
            this.orderTypes = pairSetting.orderTypes
            this.updateDate = LocalDateTime.now()
        }
        return pairSettingRepository.save(pairSetting).awaitFirst().toPairSetting().also {
            cacheManager.put(
                "pair-setting:${it.pair}",
                it,
                5, TimeUnit.MINUTES
            )
        }
    }
}